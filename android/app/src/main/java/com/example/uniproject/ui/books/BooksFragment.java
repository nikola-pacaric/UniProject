package com.example.uniproject.ui.books;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.uniproject.R;

public final class BooksFragment extends Fragment {
    public BooksFragment() {
        super(R.layout.fragment_feature_placeholder);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView title = view.findViewById(R.id.featureTitleText);
        TextView description = view.findViewById(R.id.featureDescriptionText);
        title.setText(R.string.books_title);
        description.setText(R.string.books_description);
    }
}
