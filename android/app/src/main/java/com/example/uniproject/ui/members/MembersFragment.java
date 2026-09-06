package com.example.uniproject.ui.members;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uniproject.R;
import com.example.uniproject.data.http.ApiErrorResponse;
import com.example.uniproject.data.model.member.MemberResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class MembersFragment extends Fragment {
    private static final int MEMBER_NAME_MAX_LENGTH = 100;
    private static final int MEMBER_CARD_NUMBER_MAX_LENGTH = 50;
    private static final int MEMBER_EMAIL_MAX_LENGTH = 150;
    private static final int MEMBER_PHONE_MAX_LENGTH = 50;

    private MembersViewModel viewModel;
    private MemberAdapter adapter;
    private RecyclerView membersRecyclerView;
    private ProgressBar membersProgress;
    private TextView membersEmptyText;
    private View membersErrorContainer;
    private TextView membersErrorText;
    private MaterialButton membersRetryButton;
    private FloatingActionButton addMemberButton;

    private AlertDialog memberFormDialog;
    private TextInputLayout memberFirstNameInputLayout;
    private TextInputEditText memberFirstNameInput;
    private TextInputLayout memberLastNameInputLayout;
    private TextInputEditText memberLastNameInput;
    private TextInputLayout memberCardNumberInputLayout;
    private TextInputEditText memberCardNumberInput;
    private TextInputLayout memberEmailInputLayout;
    private TextInputEditText memberEmailInput;
    private TextInputLayout memberPhoneInputLayout;
    private TextInputEditText memberPhoneInput;
    private TextView memberFormErrorText;
    private ProgressBar memberFormProgress;
    private MemberResponse editingMember;

    public MembersFragment() {
        super(R.layout.fragment_members);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MembersViewModel.class);
        bindViews(view);
        configureList();
        observeViewModel();

        membersRetryButton.setOnClickListener(ignored -> viewModel.loadMembers());
        addMemberButton.setOnClickListener(ignored -> showCreateMemberDialog());

        if (viewModel.getMembers().getValue() == null
                && viewModel.getLoadError().getValue() == null) {
            viewModel.loadMembers();
        }
        updateMemberActionState();
    }

    private void bindViews(View view) {
        membersRecyclerView = view.findViewById(R.id.membersRecyclerView);
        membersProgress = view.findViewById(R.id.membersProgress);
        membersEmptyText = view.findViewById(R.id.membersEmptyText);
        membersErrorContainer = view.findViewById(R.id.membersErrorContainer);
        membersErrorText = view.findViewById(R.id.membersErrorText);
        membersRetryButton = view.findViewById(R.id.membersRetryButton);
        addMemberButton = view.findViewById(R.id.addMemberButton);
    }

    private void configureList() {
        adapter = new MemberAdapter(
                this::openMemberLoanHistory,
                this::showEditMemberDialog,
                this::showStatusChangeConfirmation
        );
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        membersRecyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), this::renderLoading);
        viewModel.getMembers().observe(getViewLifecycleOwner(), loadedMembers -> {
            if (loadedMembers != null) {
                renderMembers(loadedMembers);
            }
        });
        viewModel.getLoadError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                renderError(error);
            }
        });
        viewModel.getCreating().observe(getViewLifecycleOwner(), this::renderSaving);
        viewModel.getCreateError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                renderMemberFormError(error);
            }
        });
        viewModel.getCreateSuccess().observe(getViewLifecycleOwner(), member -> {
            if (member != null) {
                renderCreateSuccess(member);
            }
        });
        viewModel.getUpdating().observe(getViewLifecycleOwner(), this::renderSaving);
        viewModel.getUpdateError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                renderMemberFormError(error);
            }
        });
        viewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), member -> {
            if (member != null) {
                renderUpdateSuccess(member);
            }
        });
        viewModel.getStatusChangingMemberId().observe(getViewLifecycleOwner(), memberId -> {
            adapter.setStatusChangingMemberId(memberId);
            updateMemberActionState();
        });
        viewModel.getStatusChangeSuccess().observe(getViewLifecycleOwner(), member -> {
            if (member != null) {
                renderStatusChangeSuccess(member);
            }
        });
        viewModel.getStatusChangeError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                renderStatusChangeError(error);
            }
        });
    }

    private void renderLoading(Boolean loadingValue) {
        boolean loading = Boolean.TRUE.equals(loadingValue);
        membersProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            membersRecyclerView.setVisibility(View.GONE);
            membersEmptyText.setVisibility(View.GONE);
            membersErrorContainer.setVisibility(View.GONE);
        }
        updateMemberActionState();
    }

    private void renderMembers(List<MemberResponse> loadedMembers) {
        adapter.submitList(loadedMembers);
        membersProgress.setVisibility(View.GONE);
        membersErrorContainer.setVisibility(View.GONE);

        boolean empty = loadedMembers.isEmpty();
        membersEmptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
        membersRecyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void renderError(ApiErrorResponse error) {
        membersProgress.setVisibility(View.GONE);
        membersRecyclerView.setVisibility(View.GONE);
        membersEmptyText.setVisibility(View.GONE);
        membersErrorContainer.setVisibility(View.VISIBLE);

        String message = error.getMessage();
        membersErrorText.setText(TextUtils.isEmpty(message)
                ? getString(R.string.members_load_failed)
                : message);
    }

    private void showCreateMemberDialog() {
        showMemberFormDialog(null);
    }

    private void openMemberLoanHistory(MemberResponse member) {
        if (member.getId() == null) {
            return;
        }

        Bundle arguments = new Bundle();
        arguments.putLong(MemberLoanHistoryFragment.ARG_MEMBER_ID, member.getId());
        NavHostFragment.findNavController(this).navigate(
                R.id.action_membersFragment_to_memberLoanHistoryFragment,
                arguments
        );
    }

    private void showEditMemberDialog(MemberResponse member) {
        showMemberFormDialog(member);
    }

    private void showMemberFormDialog(@Nullable MemberResponse member) {
        if (memberFormDialog != null && memberFormDialog.isShowing()) {
            return;
        }
        if (member != null && member.getId() == null) {
            return;
        }

        editingMember = member;
        if (editingMember == null) {
            viewModel.prepareCreate();
        } else {
            viewModel.prepareUpdate();
        }

        View formView = getLayoutInflater().inflate(R.layout.dialog_member_form, null, false);
        bindMemberForm(formView);
        populateMemberForm(editingMember);

        memberFormDialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(editingMember == null
                        ? R.string.create_member_title
                        : R.string.edit_member_title)
                .setView(formView)
                .setNegativeButton(R.string.cancel_action, null)
                .setPositiveButton(R.string.member_save_action, null)
                .create();

        memberFormDialog.setOnShowListener(ignored -> {
            Button saveButton = memberFormDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            saveButton.setOnClickListener(button -> attemptSaveMember());
            renderSaving(null);
            memberFirstNameInput.requestFocus();
            memberFirstNameInput.setSelection(memberFirstNameInput.length());
        });
        memberFormDialog.setOnDismissListener(ignored -> clearMemberFormReferences());
        memberFormDialog.show();
    }

    private void bindMemberForm(View formView) {
        memberFirstNameInputLayout = formView.findViewById(R.id.memberFirstNameInputLayout);
        memberFirstNameInput = formView.findViewById(R.id.memberFirstNameInput);
        memberLastNameInputLayout = formView.findViewById(R.id.memberLastNameInputLayout);
        memberLastNameInput = formView.findViewById(R.id.memberLastNameInput);
        memberCardNumberInputLayout = formView.findViewById(R.id.memberCardNumberInputLayout);
        memberCardNumberInput = formView.findViewById(R.id.memberCardNumberInput);
        memberEmailInputLayout = formView.findViewById(R.id.memberEmailInputLayout);
        memberEmailInput = formView.findViewById(R.id.memberEmailInput);
        memberPhoneInputLayout = formView.findViewById(R.id.memberPhoneInputLayout);
        memberPhoneInput = formView.findViewById(R.id.memberPhoneInput);
        memberFormErrorText = formView.findViewById(R.id.memberFormErrorText);
        memberFormProgress = formView.findViewById(R.id.memberFormProgress);
    }

    private void populateMemberForm(@Nullable MemberResponse member) {
        if (member == null) {
            return;
        }

        memberFirstNameInput.setText(member.getFirstName());
        memberLastNameInput.setText(member.getLastName());
        memberCardNumberInput.setText(member.getMembershipCardNumber());
        memberEmailInput.setText(member.getEmail());
        memberPhoneInput.setText(member.getPhone());
    }

    private void attemptSaveMember() {
        clearMemberFormErrors();

        String firstName = textOf(memberFirstNameInput).trim();
        String lastName = textOf(memberLastNameInput).trim();
        String cardNumber = textOf(memberCardNumberInput).trim();
        String email = textOf(memberEmailInput).trim();
        String phone = textOf(memberPhoneInput).trim();

        if (!validateRequiredText(
                firstName,
                MEMBER_NAME_MAX_LENGTH,
                memberFirstNameInputLayout,
                memberFirstNameInput,
                R.string.member_first_name_required,
                R.string.member_name_length
        )) {
            return;
        }
        if (!validateRequiredText(
                lastName,
                MEMBER_NAME_MAX_LENGTH,
                memberLastNameInputLayout,
                memberLastNameInput,
                R.string.member_last_name_required,
                R.string.member_name_length
        )) {
            return;
        }
        if (!validateRequiredText(
                cardNumber,
                MEMBER_CARD_NUMBER_MAX_LENGTH,
                memberCardNumberInputLayout,
                memberCardNumberInput,
                R.string.member_card_number_required,
                R.string.member_card_number_length
        )) {
            return;
        }
        if (!validateEmail(email)) {
            return;
        }
        if (phone.length() > MEMBER_PHONE_MAX_LENGTH) {
            memberPhoneInputLayout.setError(getString(R.string.member_phone_length));
            memberPhoneInput.requestFocus();
            return;
        }

        hideKeyboard();
        String optionalPhone = TextUtils.isEmpty(phone) ? null : phone;
        if (editingMember == null) {
            viewModel.createMember(firstName, lastName, cardNumber, email, optionalPhone);
        } else {
            viewModel.updateMember(
                    editingMember.getId(),
                    firstName,
                    lastName,
                    cardNumber,
                    email,
                    optionalPhone
            );
        }
    }

    private boolean validateRequiredText(
            String value,
            int maxLength,
            TextInputLayout inputLayout,
            TextInputEditText input,
            int requiredMessage,
            int lengthMessage
    ) {
        if (TextUtils.isEmpty(value)) {
            inputLayout.setError(getString(requiredMessage));
            input.requestFocus();
            return false;
        }
        if (value.length() > maxLength) {
            inputLayout.setError(getString(lengthMessage));
            input.requestFocus();
            return false;
        }
        return true;
    }

    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            memberEmailInputLayout.setError(getString(R.string.member_email_required));
            memberEmailInput.requestFocus();
            return false;
        }
        if (email.length() > MEMBER_EMAIL_MAX_LENGTH) {
            memberEmailInputLayout.setError(getString(R.string.member_email_length));
            memberEmailInput.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            memberEmailInputLayout.setError(getString(R.string.member_email_invalid));
            memberEmailInput.requestFocus();
            return false;
        }
        return true;
    }

    private void renderSaving(Boolean ignored) {
        updateMemberActionState();
        if (memberFormDialog == null || !memberFormDialog.isShowing()) {
            return;
        }

        boolean saving = Boolean.TRUE.equals(viewModel.getCreating().getValue())
                || Boolean.TRUE.equals(viewModel.getUpdating().getValue());
        memberFirstNameInput.setEnabled(!saving);
        memberLastNameInput.setEnabled(!saving);
        memberCardNumberInput.setEnabled(!saving);
        memberEmailInput.setEnabled(!saving);
        memberPhoneInput.setEnabled(!saving);
        memberFormProgress.setVisibility(saving ? View.VISIBLE : View.GONE);
        memberFormDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(!saving);
        memberFormDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(!saving);
        memberFormDialog.setCanceledOnTouchOutside(!saving);
    }

    private void renderMemberFormError(ApiErrorResponse error) {
        if (memberFormDialog == null || !memberFormDialog.isShowing()) {
            return;
        }

        clearMemberFormErrors();
        if (error.getStatus() == 409) {
            memberCardNumberInputLayout.setError(getString(
                    R.string.member_card_number_duplicate
            ));
            memberCardNumberInput.requestFocus();
            return;
        }

        Map<String, String> fieldErrors = error.getFieldErrors();
        if (fieldErrors != null) {
            setMemberFieldErrors(fieldErrors);
        }

        String message = error.getMessage();
        memberFormErrorText.setText(TextUtils.isEmpty(message)
                ? getString(R.string.member_save_failed)
                : message);
        memberFormErrorText.setVisibility(View.VISIBLE);
    }

    private void setMemberFieldErrors(Map<String, String> fieldErrors) {
        setRequiredOrLengthError(
                fieldErrors.get("firstName"),
                memberFirstNameInputLayout,
                R.string.member_first_name_required,
                R.string.member_name_length
        );
        setRequiredOrLengthError(
                fieldErrors.get("lastName"),
                memberLastNameInputLayout,
                R.string.member_last_name_required,
                R.string.member_name_length
        );
        setRequiredOrLengthError(
                fieldErrors.get("membershipCardNumber"),
                memberCardNumberInputLayout,
                R.string.member_card_number_required,
                R.string.member_card_number_length
        );

        String emailError = fieldErrors.get("email");
        if (!TextUtils.isEmpty(emailError)) {
            String lowerCaseError = emailError.toLowerCase(Locale.ROOT);
            int errorResource;
            if (isRequiredError(lowerCaseError)) {
                errorResource = R.string.member_email_required;
            } else if (lowerCaseError.contains("valid")) {
                errorResource = R.string.member_email_invalid;
            } else {
                errorResource = R.string.member_email_length;
            }
            memberEmailInputLayout.setError(getString(errorResource));
        }

        if (!TextUtils.isEmpty(fieldErrors.get("phone"))) {
            memberPhoneInputLayout.setError(getString(R.string.member_phone_length));
        }
    }

    private void setRequiredOrLengthError(
            String backendError,
            TextInputLayout inputLayout,
            int requiredMessage,
            int lengthMessage
    ) {
        if (TextUtils.isEmpty(backendError)) {
            return;
        }
        inputLayout.setError(getString(isRequiredError(backendError)
                ? requiredMessage
                : lengthMessage));
    }

    private boolean isRequiredError(String backendError) {
        String normalizedError = backendError.toLowerCase(Locale.ROOT);
        return normalizedError.contains("required") || normalizedError.contains("blank");
    }

    private void renderCreateSuccess(MemberResponse member) {
        viewModel.consumeCreateSuccess();
        renderMemberMutationSuccess(member, R.string.member_create_success);
    }

    private void renderUpdateSuccess(MemberResponse member) {
        viewModel.consumeUpdateSuccess();
        renderMemberMutationSuccess(member, R.string.member_update_success);
    }

    private void renderMemberMutationSuccess(MemberResponse member, int messageResource) {
        if (memberFormDialog != null) {
            memberFormDialog.dismiss();
        }

        Snackbar.make(
                requireView(),
                getString(messageResource, memberName(member)),
                Snackbar.LENGTH_LONG
        ).show();
        viewModel.loadMembers();
    }

    private void showStatusChangeConfirmation(MemberResponse member) {
        if (!Boolean.TRUE.equals(member.getActive())) {
            viewModel.activateMember(member);
            return;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.deactivate_member_title)
                .setMessage(getString(
                        R.string.deactivate_member_confirmation,
                        memberName(member)
                ))
                .setNegativeButton(R.string.cancel_action, null)
                .setPositiveButton(
                        R.string.deactivate_member_action,
                        (dialog, which) -> viewModel.deactivateMember(member)
                )
                .show();
    }

    private void renderStatusChangeSuccess(MemberResponse member) {
        viewModel.consumeStatusChangeSuccess();
        Snackbar.make(
                requireView(),
                Boolean.TRUE.equals(member.getActive())
                        ? R.string.member_activate_success
                        : R.string.member_deactivate_success,
                Snackbar.LENGTH_LONG
        ).show();
        viewModel.loadMembers();
    }

    private void renderStatusChangeError(ApiErrorResponse error) {
        viewModel.consumeStatusChangeError();
        String message = error.getMessage();
        Snackbar.make(
                requireView(),
                TextUtils.isEmpty(message)
                        ? getString(R.string.member_status_change_failed)
                        : message,
                Snackbar.LENGTH_LONG
        ).show();
    }

    private void updateMemberActionState() {
        if (adapter == null || addMemberButton == null) {
            return;
        }

        boolean loading = Boolean.TRUE.equals(viewModel.getLoading().getValue());
        boolean creating = Boolean.TRUE.equals(viewModel.getCreating().getValue());
        boolean updating = Boolean.TRUE.equals(viewModel.getUpdating().getValue());
        boolean changingStatus = viewModel.getStatusChangingMemberId().getValue() != null;
        boolean actionsEnabled = !loading && !creating && !updating && !changingStatus;
        adapter.setActionsEnabled(actionsEnabled);
        addMemberButton.setEnabled(actionsEnabled);
    }

    private void clearMemberFormErrors() {
        memberFirstNameInputLayout.setError(null);
        memberLastNameInputLayout.setError(null);
        memberCardNumberInputLayout.setError(null);
        memberEmailInputLayout.setError(null);
        memberPhoneInputLayout.setError(null);
        memberFormErrorText.setText(null);
        memberFormErrorText.setVisibility(View.GONE);
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }

    private String memberName(MemberResponse member) {
        String firstName = member.getFirstName() == null ? "" : member.getFirstName();
        String lastName = member.getLastName() == null ? "" : member.getLastName();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isEmpty() ? getString(R.string.member_name_missing) : fullName;
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusedView = memberFormDialog == null
                ? null
                : memberFormDialog.getCurrentFocus();
        if (focusedView != null) {
            inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
            focusedView.clearFocus();
        }
    }

    private void clearMemberFormReferences() {
        memberFormDialog = null;
        memberFirstNameInputLayout = null;
        memberFirstNameInput = null;
        memberLastNameInputLayout = null;
        memberLastNameInput = null;
        memberCardNumberInputLayout = null;
        memberCardNumberInput = null;
        memberEmailInputLayout = null;
        memberEmailInput = null;
        memberPhoneInputLayout = null;
        memberPhoneInput = null;
        memberFormErrorText = null;
        memberFormProgress = null;
        editingMember = null;
    }

    @Override
    public void onDestroyView() {
        if (memberFormDialog != null) {
            memberFormDialog.dismiss();
        }
        membersRecyclerView.setAdapter(null);
        adapter = null;
        membersRecyclerView = null;
        membersProgress = null;
        membersEmptyText = null;
        membersErrorContainer = null;
        membersErrorText = null;
        membersRetryButton = null;
        addMemberButton = null;
        super.onDestroyView();
    }
}
