package com.example.uniproject.ui.books;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uniproject.R;
import com.example.uniproject.data.model.author.AuthorResponse;
import com.example.uniproject.data.model.book.BookResponse;
import com.example.uniproject.data.model.category.CategoryResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private final OnBookEditClickListener editClickListener;
    private final OnBookDeleteClickListener deleteClickListener;
    private List<BookResponse> books = Collections.emptyList();
    private Map<Long, String> authorNames = Collections.emptyMap();
    private Map<Long, String> categoryNames = Collections.emptyMap();
    private boolean actionsEnabled = true;

    public BookAdapter(
            OnBookEditClickListener editClickListener,
            OnBookDeleteClickListener deleteClickListener
    ) {
        this.editClickListener = editClickListener;
        this.deleteClickListener = deleteClickListener;
        setHasStableIds(true);
    }

    public void submitList(List<BookResponse> books) {
        this.books = books == null
                ? Collections.emptyList()
                : new ArrayList<>(books);
        notifyDataSetChanged();
    }

    public void submitAuthors(List<AuthorResponse> authors) {
        Map<Long, String> names = new HashMap<>();
        if (authors != null) {
            for (AuthorResponse author : authors) {
                if (author.getId() == null) {
                    continue;
                }

                String firstName = author.getFirstName() == null ? "" : author.getFirstName();
                String lastName = author.getLastName() == null ? "" : author.getLastName();
                String fullName = (firstName + " " + lastName).trim();
                if (!fullName.isEmpty()) {
                    names.put(author.getId(), fullName);
                }
            }
        }
        authorNames = names;
        notifyDataSetChanged();
    }

    public void submitCategories(List<CategoryResponse> categories) {
        Map<Long, String> names = new HashMap<>();
        if (categories != null) {
            for (CategoryResponse category : categories) {
                if (category.getId() == null || category.getName() == null) {
                    continue;
                }

                String name = category.getName().trim();
                if (!name.isEmpty()) {
                    names.put(category.getId(), name);
                }
            }
        }
        categoryNames = names;
        notifyDataSetChanged();
    }

    public void setActionsEnabled(boolean enabled) {
        if (actionsEnabled == enabled) {
            return;
        }
        actionsEnabled = enabled;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        Long id = books.get(position).getId();
        return id == null ? RecyclerView.NO_ID : id;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        BookResponse book = books.get(position);
        String authorName = authorNames.get(book.getAuthorId());
        String categoryName = categoryNames.get(book.getCategoryId());
        holder.bind(
                book,
                authorName,
                categoryName,
                actionsEnabled,
                editClickListener,
                deleteClickListener
        );
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static final class BookViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView metadataText;
        private final TextView authorText;
        private final TextView categoryText;
        private final TextView availabilityText;
        private final ImageButton editButton;
        private final ImageButton deleteButton;

        BookViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.bookTitleText);
            metadataText = itemView.findViewById(R.id.bookMetadataText);
            authorText = itemView.findViewById(R.id.bookAuthorText);
            categoryText = itemView.findViewById(R.id.bookCategoryText);
            availabilityText = itemView.findViewById(R.id.bookAvailabilityText);
            editButton = itemView.findViewById(R.id.editBookButton);
            deleteButton = itemView.findViewById(R.id.deleteBookButton);
        }

        void bind(
                BookResponse book,
                String authorName,
                String categoryName,
                boolean actionsEnabled,
                OnBookEditClickListener editClickListener,
                OnBookDeleteClickListener deleteClickListener
        ) {
            String title = book.getTitle() == null ? "" : book.getTitle();
            if (title.trim().isEmpty()) {
                title = itemView.getContext().getString(R.string.book_title_missing);
            }
            titleText.setText(title);

            String isbn = book.getIsbn() == null ? "" : book.getIsbn();
            String publicationYear = book.getPublicationYear() == null
                    ? itemView.getContext().getString(R.string.book_value_missing)
                    : String.valueOf(book.getPublicationYear());
            metadataText.setText(itemView.getContext().getString(
                    R.string.book_metadata_value,
                    isbn,
                    publicationYear
            ));

            String displayedAuthor = authorName == null
                    ? itemView.getContext().getString(R.string.book_author_missing)
                    : authorName;
            authorText.setText(itemView.getContext().getString(
                    R.string.book_author_value,
                    displayedAuthor
            ));

            String displayedCategory = categoryName == null
                    ? itemView.getContext().getString(R.string.book_category_missing)
                    : categoryName;
            categoryText.setText(itemView.getContext().getString(
                    R.string.book_category_value,
                    displayedCategory
            ));

            int availableCopies = book.getAvailableCopies() == null
                    ? 0
                    : book.getAvailableCopies();
            int totalCopies = book.getTotalCopies() == null ? 0 : book.getTotalCopies();
            availabilityText.setText(itemView.getContext().getString(
                    R.string.book_availability_value,
                    availableCopies,
                    totalCopies
            ));

            editButton.setContentDescription(itemView.getContext().getString(
                    R.string.edit_book_content_description,
                    title
            ));
            editButton.setEnabled(actionsEnabled);
            editButton.setOnClickListener(ignored -> editClickListener.onEditClick(book));

            deleteButton.setContentDescription(itemView.getContext().getString(
                    R.string.delete_book_content_description,
                    title
            ));
            deleteButton.setEnabled(actionsEnabled);
            deleteButton.setOnClickListener(ignored -> deleteClickListener.onDeleteClick(book));
        }
    }

    public interface OnBookEditClickListener {
        void onEditClick(BookResponse book);
    }

    public interface OnBookDeleteClickListener {
        void onDeleteClick(BookResponse book);
    }
}
