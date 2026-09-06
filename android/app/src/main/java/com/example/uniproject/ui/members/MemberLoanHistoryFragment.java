package com.example.uniproject.ui.members;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uniproject.R;
import com.example.uniproject.data.http.ApiErrorResponse;
import com.example.uniproject.data.model.loan.LoanResponse;
import com.example.uniproject.data.model.member.MemberResponse;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public final class MemberLoanHistoryFragment extends Fragment {
    public static final String ARG_MEMBER_ID = "memberId";

    private MemberLoanHistoryViewModel viewModel;
    private MemberLoanHistoryAdapter adapter;
    private View memberSummaryCard;
    private TextView memberNameText;
    private TextView memberCardNumberText;
    private TextView memberStatusText;
    private ProgressBar historyProgress;
    private RecyclerView historyRecyclerView;
    private TextView historyEmptyText;
    private View historyErrorContainer;
    private TextView historyErrorText;
    private MaterialButton historyRetryButton;
    private MaterialButton historyBackButton;
    private long memberId;

    public MemberLoanHistoryFragment() {
        super(R.layout.fragment_member_loan_history);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MemberLoanHistoryViewModel.class);
        bindViews(view);
        configureList();
        observeViewModel();

        historyBackButton.setOnClickListener(ignored ->
                NavHostFragment.findNavController(this).navigateUp());
        historyRetryButton.setOnClickListener(ignored -> viewModel.reloadHistory());

        Bundle arguments = getArguments();
        memberId = arguments == null ? -1L : arguments.getLong(ARG_MEMBER_ID, -1L);
        if (memberId <= 0L) {
            renderInvalidMember();
            return;
        }

        Long loadedMemberId = viewModel.getCurrentMemberId();
        if (loadedMemberId == null || loadedMemberId != memberId) {
            viewModel.loadHistory(memberId);
        }
    }

    private void bindViews(View view) {
        memberSummaryCard = view.findViewById(R.id.memberHistorySummaryCard);
        memberNameText = view.findViewById(R.id.memberHistoryNameText);
        memberCardNumberText = view.findViewById(R.id.memberHistoryCardNumberText);
        memberStatusText = view.findViewById(R.id.memberHistoryMemberStatusText);
        historyProgress = view.findViewById(R.id.memberHistoryProgress);
        historyRecyclerView = view.findViewById(R.id.memberHistoryRecyclerView);
        historyEmptyText = view.findViewById(R.id.memberHistoryEmptyText);
        historyErrorContainer = view.findViewById(R.id.memberHistoryErrorContainer);
        historyErrorText = view.findViewById(R.id.memberHistoryErrorText);
        historyRetryButton = view.findViewById(R.id.memberHistoryRetryButton);
        historyBackButton = view.findViewById(R.id.memberHistoryBackButton);
    }

    private void configureList() {
        adapter = new MemberLoanHistoryAdapter();
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        historyRecyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), ignored -> renderState());
        viewModel.getMember().observe(getViewLifecycleOwner(), ignored -> renderState());
        viewModel.getLoans().observe(getViewLifecycleOwner(), ignored -> renderState());
        viewModel.getLoadError().observe(getViewLifecycleOwner(), ignored -> renderState());
    }

    private void renderState() {
        if (Boolean.TRUE.equals(viewModel.getLoading().getValue())) {
            renderLoading();
            return;
        }

        ApiErrorResponse error = viewModel.getLoadError().getValue();
        if (error != null) {
            renderError(error);
            return;
        }

        MemberResponse member = viewModel.getMember().getValue();
        List<LoanResponse> loans = viewModel.getLoans().getValue();
        if (member == null || loans == null) {
            return;
        }

        renderContent(member, loans);
    }

    private void renderLoading() {
        historyProgress.setVisibility(View.VISIBLE);
        memberSummaryCard.setVisibility(View.GONE);
        historyRecyclerView.setVisibility(View.GONE);
        historyEmptyText.setVisibility(View.GONE);
        historyErrorContainer.setVisibility(View.GONE);
    }

    private void renderContent(MemberResponse member, List<LoanResponse> loans) {
        historyProgress.setVisibility(View.GONE);
        historyErrorContainer.setVisibility(View.GONE);
        memberSummaryCard.setVisibility(View.VISIBLE);

        memberNameText.setText(memberName(member));
        memberCardNumberText.setText(getString(
                R.string.member_card_number_value,
                valueOrMissing(member.getMembershipCardNumber())
        ));
        memberStatusText.setText(Boolean.TRUE.equals(member.getActive())
                ? R.string.member_history_member_active
                : R.string.member_history_member_inactive);

        adapter.submitList(loans);
        boolean empty = loans.isEmpty();
        historyEmptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
        historyRecyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void renderError(ApiErrorResponse error) {
        historyProgress.setVisibility(View.GONE);
        memberSummaryCard.setVisibility(View.GONE);
        historyRecyclerView.setVisibility(View.GONE);
        historyEmptyText.setVisibility(View.GONE);
        historyErrorContainer.setVisibility(View.VISIBLE);
        historyRetryButton.setVisibility(View.VISIBLE);

        String message = error.getMessage();
        historyErrorText.setText(TextUtils.isEmpty(message)
                ? getString(R.string.member_history_load_failed)
                : message);
    }

    private void renderInvalidMember() {
        historyProgress.setVisibility(View.GONE);
        memberSummaryCard.setVisibility(View.GONE);
        historyRecyclerView.setVisibility(View.GONE);
        historyEmptyText.setVisibility(View.GONE);
        historyErrorContainer.setVisibility(View.VISIBLE);
        historyErrorText.setText(R.string.member_history_invalid_member);
        historyRetryButton.setVisibility(View.GONE);
    }

    private String memberName(MemberResponse member) {
        String firstName = member.getFirstName() == null ? "" : member.getFirstName();
        String lastName = member.getLastName() == null ? "" : member.getLastName();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isEmpty() ? getString(R.string.member_name_missing) : fullName;
    }

    private String valueOrMissing(String value) {
        return value == null || value.trim().isEmpty()
                ? getString(R.string.member_value_missing)
                : value;
    }

    @Override
    public void onDestroyView() {
        historyRecyclerView.setAdapter(null);
        adapter = null;
        memberSummaryCard = null;
        memberNameText = null;
        memberCardNumberText = null;
        memberStatusText = null;
        historyProgress = null;
        historyRecyclerView = null;
        historyEmptyText = null;
        historyErrorContainer = null;
        historyErrorText = null;
        historyRetryButton = null;
        historyBackButton = null;
        super.onDestroyView();
    }
}
