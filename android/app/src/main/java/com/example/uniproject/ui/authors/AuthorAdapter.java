package com.example.uniproject.ui.authors;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uniproject.R;
import com.example.uniproject.data.model.author.AuthorResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AuthorAdapter extends RecyclerView.Adapter<AuthorAdapter.AuthorViewHolder> {
    private final OnAuthorEditClickListener editClickListener;
    private final OnAuthorDeleteClickListener deleteClickListener;
    private List<AuthorResponse> authors = Collections.emptyList();
    private boolean actionsEnabled = true;

    public AuthorAdapter(
            OnAuthorEditClickListener editClickListener,
            OnAuthorDeleteClickListener deleteClickListener
    ) {
        this.editClickListener = editClickListener;
        this.deleteClickListener = deleteClickListener;
        setHasStableIds(true);
    }

    public void submitList(List<AuthorResponse> authors) {
        this.authors = authors == null
                ? Collections.emptyList()
                : new ArrayList<>(authors);
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
        Long id = authors.get(position).getId();
        return id == null ? RecyclerView.NO_ID : id;
    }

    @NonNull
    @Override
    public AuthorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_author, parent, false);
        return new AuthorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AuthorViewHolder holder, int position) {
        holder.bind(
                authors.get(position),
                actionsEnabled,
                editClickListener,
                deleteClickListener
        );
    }

    @Override
    public int getItemCount() {
        return authors.size();
    }

    static final class AuthorViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView biographyText;
        private final ImageButton editButton;
        private final ImageButton deleteButton;

        AuthorViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.authorNameText);
            biographyText = itemView.findViewById(R.id.authorBiographyText);
            editButton = itemView.findViewById(R.id.editAuthorButton);
            deleteButton = itemView.findViewById(R.id.deleteAuthorButton);
        }

        void bind(
                AuthorResponse author,
                boolean actionsEnabled,
                OnAuthorEditClickListener editClickListener,
                OnAuthorDeleteClickListener deleteClickListener
        ) {
            String firstName = author.getFirstName() == null ? "" : author.getFirstName();
            String lastName = author.getLastName() == null ? "" : author.getLastName();
            String fullName = (firstName + " " + lastName).trim();
            nameText.setText(fullName);

            String biography = author.getBiography();
            biographyText.setText(TextUtils.isEmpty(biography)
                    ? itemView.getContext().getString(R.string.author_biography_missing)
                    : biography);
            editButton.setContentDescription(itemView.getContext().getString(
                    R.string.edit_author_content_description,
                    fullName
            ));
            editButton.setEnabled(actionsEnabled);
            editButton.setOnClickListener(ignored -> editClickListener.onEditClick(author));
            deleteButton.setContentDescription(itemView.getContext().getString(
                    R.string.delete_author_content_description,
                    fullName
            ));
            deleteButton.setEnabled(actionsEnabled);
            deleteButton.setOnClickListener(ignored -> deleteClickListener.onDeleteClick(author));
        }
    }

    public interface OnAuthorEditClickListener {
        void onEditClick(AuthorResponse author);
    }

    public interface OnAuthorDeleteClickListener {
        void onDeleteClick(AuthorResponse author);
    }
}
