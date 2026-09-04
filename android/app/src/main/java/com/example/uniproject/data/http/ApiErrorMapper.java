package com.example.uniproject.data.http;

import com.example.uniproject.data.remote.RetrofitProvider;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.SocketTimeoutException;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;

public final class ApiErrorMapper {
    private ApiErrorMapper() {
    }

    public static ApiErrorResponse fromResponse(Response<?> response) {
        ApiErrorResponse apiError = parseErrorBody(response.errorBody());

        if (apiError == null) {
            apiError = new ApiErrorResponse();
        }

        apiError.setStatus(response.code());
        apiError.setMessage(toUserMessage(apiError.getMessage(), response.code()));
        return apiError;
    }

    public static ApiErrorResponse fromThrowable(Throwable throwable) {
        ApiErrorResponse apiError = new ApiErrorResponse();
        apiError.setStatus(0);

        if (throwable instanceof SocketTimeoutException) {
            apiError.setMessage("Zahtev je istekao. Pokušajte ponovo.");
        } else if (throwable instanceof IOException) {
            apiError.setMessage("Server nije dostupan. Proverite da li je pokrenut.");
        } else {
            apiError.setMessage("Dogodila se neočekivana greška.");
        }

        return apiError;
    }

    private static ApiErrorResponse parseErrorBody(ResponseBody errorBody) {
        if (errorBody == null) {
            return null;
        }

        Converter<ResponseBody, ApiErrorResponse> converter = RetrofitProvider.getRetrofit()
                .responseBodyConverter(ApiErrorResponse.class, new Annotation[0]);

        try (ResponseBody body = errorBody) {
            return converter.convert(body);
        } catch (IOException | RuntimeException ignored) {
            return null;
        }
    }

    private static String toUserMessage(String backendMessage, int status) {
        if (backendMessage != null) {
            switch (backendMessage) {
                case "Username already taken":
                    return "Korisničko ime je već zauzeto.";
                case "Email already registered":
                    return "Email adresa je već registrovana.";
                case "Invalid username or password":
                    return "Pogrešno korisničko ime ili lozinka.";
                case "Validation failed":
                    return "Uneti podaci nisu ispravni.";
                case "Cannot delete author with associated books.":
                    return "Autor ne može biti obrisan jer ima povezane knjige.";
                case "Cannot delete category with associated books.":
                    return "Kategorija ne može biti obrisana jer ima povezane knjige.";
                case "Operation conflicts with existing data or related records.":
                    return "Operacija nije moguća zbog postojećih ili povezanih podataka.";
                default:
                    break;
            }

            if (backendMessage.startsWith("Cannot set total copies")) {
                return "Ukupan broj primeraka ne može biti manji od broja trenutno zaduženih primeraka.";
            }
            if (backendMessage.startsWith("Cannot delete book")) {
                return "Knjiga ne može biti obrisana jer ima aktivna zaduženja.";
            }
            if (backendMessage.startsWith("Cannot loan to inactive member")) {
                return "Knjiga se ne može zadužiti neaktivnom članu.";
            }
            if (backendMessage.startsWith("No available copies")) {
                return "Nema dostupnih primeraka izabrane knjige.";
            }
            if (backendMessage.startsWith("Loan with id")
                    && backendMessage.contains("already returned")) {
                return "Ovo zaduženje je već vraćeno.";
            }
            if (backendMessage.startsWith("Cannot return loan")) {
                return "Vraćanje ovog zaduženja trenutno nije moguće.";
            }
        }

        switch (status) {
            case 400:
                return "Zahtev nije moguće izvršiti. Proverite unete podatke.";
            case 401:
                return "Sesija nije validna. Prijavite se ponovo.";
            case 403:
                return "Nemate dozvolu za ovu operaciju.";
            case 404:
                return "Traženi podatak nije pronađen.";
            case 409:
                return "Operacija nije moguća zbog postojećih ili povezanih podataka.";
            default:
                break;
        }

        if (status >= 500) {
            return "Došlo je do greške na serveru. Pokušajte ponovo.";
        }

        if (backendMessage != null && !backendMessage.trim().isEmpty()) {
            return backendMessage;
        }

        return "Zahtev nije uspeo. Pokušajte ponovo.";
    }
}
