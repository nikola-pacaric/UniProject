package com.example.uniproject.ui.members;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uniproject.R;
import com.example.uniproject.data.model.loan.LoanResponse;
import com.google.android.material.color.MaterialColors;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class MemberLoanHistoryAdapter
        extends RecyclerView.Adapter<MemberLoanHistoryAdapter.LoanViewHolder> {
    private List<LoanResponse> loans = Collections.emptyList();

    public MemberLoanHistoryAdapter() {
        setHasStableIds(true);
    }

    public void submitList(List<LoanResponse> loans) {
        this.loans = loans == null
                ? Collections.emptyList()
                : new ArrayList<>(loans);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        Long id = loans.get(position).getId();
        return id == null ? RecyclerView.NO_ID : id;
    }

    @NonNull
    @Override
    public LoanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member_loan, parent, false);
        return new LoanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LoanViewHolder holder, int position) {
        holder.bind(loans.get(position));
    }

    @Override
    public int getItemCount() {
        return loans.size();
    }

    static final class LoanViewHolder extends RecyclerView.ViewHolder {
        private final TextView bookTitleText;
        private final TextView statusText;
        private final TextView loanDateText;
        private final TextView dueDateText;
        private final TextView returnDateText;

        LoanViewHolder(@NonNull View itemView) {
            super(itemView);
            bookTitleText = itemView.findViewById(R.id.memberLoanBookTitleText);
            statusText = itemView.findViewById(R.id.memberLoanStatusText);
            loanDateText = itemView.findViewById(R.id.memberLoanDateText);
            dueDateText = itemView.findViewById(R.id.memberLoanDueDateText);
            returnDateText = itemView.findViewById(R.id.memberLoanReturnDateText);
        }

        void bind(LoanResponse loan) {
            String bookTitle = loan.getBookTitleAtLoan();
            if (bookTitle == null || bookTitle.trim().isEmpty()) {
                bookTitleText.setText(R.string.member_history_book_missing);
            } else {
                bookTitleText.setText(bookTitle);
            }

            loanDateText.setText(itemView.getContext().getString(
                    R.string.member_history_loan_date_value,
                    formatDate(loan.getLoanDate())
            ));
            dueDateText.setText(itemView.getContext().getString(
                    R.string.member_history_due_date_value,
                    formatDate(loan.getDueDate())
            ));

            String returnedDate = loan.getReturnDate();
            returnDateText.setText(itemView.getContext().getString(
                    R.string.member_history_return_date_value,
                    returnedDate == null || returnedDate.trim().isEmpty()
                            ? itemView.getContext().getString(
                                    R.string.member_history_not_returned
                            )
                            : formatDate(returnedDate)
            ));

            renderStatus(loan);
        }

        private void renderStatus(LoanResponse loan) {
            boolean returned = "RETURNED".equals(loan.getStatus())
                    || (loan.getReturnDate() != null && !loan.getReturnDate().trim().isEmpty());
            boolean overdue = !returned && ("OVERDUE".equals(loan.getStatus())
                    || isPastDate(loan.getDueDate()));

            int statusLabel;
            int statusColorAttribute;
            if (returned) {
                statusLabel = R.string.member_history_status_returned;
                statusColorAttribute = com.google.android.material.R.attr.colorOnSurfaceVariant;
            } else if (overdue) {
                statusLabel = R.string.member_history_status_overdue;
                statusColorAttribute = com.google.android.material.R.attr.colorError;
            } else {
                statusLabel = R.string.member_history_status_active;
                statusColorAttribute = com.google.android.material.R.attr.colorPrimary;
            }

            statusText.setText(statusLabel);
            statusText.setTextColor(MaterialColors.getColor(
                    statusText,
                    statusColorAttribute,
                    Color.BLACK
            ));
        }

        private boolean isPastDate(String isoDate) {
            if (isoDate == null || isoDate.length() != 10) {
                return false;
            }
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
                    .format(new Date());
            return isoDate.compareTo(today) < 0;
        }

        private String formatDate(String isoDate) {
            if (isoDate == null || isoDate.trim().isEmpty()) {
                return itemView.getContext().getString(R.string.member_history_date_missing);
            }

            SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
            sourceFormat.setLenient(false);
            try {
                Date date = sourceFormat.parse(isoDate);
                if (date == null) {
                    return isoDate;
                }
                return new SimpleDateFormat("dd.MM.yyyy.", new Locale("sr")).format(date);
            } catch (ParseException ignored) {
                return isoDate;
            }
        }
    }
}
