package com.example.uniproject.ui.members;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uniproject.R;
import com.example.uniproject.data.model.member.MemberResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {
    private final OnMemberHistoryClickListener historyClickListener;
    private final OnMemberEditClickListener editClickListener;
    private final OnMemberStatusClickListener statusClickListener;
    private List<MemberResponse> members = Collections.emptyList();
    private boolean actionsEnabled = true;
    private Long statusChangingMemberId;

    public MemberAdapter(
            OnMemberHistoryClickListener historyClickListener,
            OnMemberEditClickListener editClickListener,
            OnMemberStatusClickListener statusClickListener
    ) {
        this.historyClickListener = historyClickListener;
        this.editClickListener = editClickListener;
        this.statusClickListener = statusClickListener;
        setHasStableIds(true);
    }

    public void submitList(List<MemberResponse> members) {
        this.members = members == null
                ? Collections.emptyList()
                : new ArrayList<>(members);
        notifyDataSetChanged();
    }

    public void setActionsEnabled(boolean enabled) {
        if (actionsEnabled == enabled) {
            return;
        }
        actionsEnabled = enabled;
        notifyDataSetChanged();
    }

    public void setStatusChangingMemberId(Long memberId) {
        if (Objects.equals(statusChangingMemberId, memberId)) {
            return;
        }
        statusChangingMemberId = memberId;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        Long id = members.get(position).getId();
        return id == null ? RecyclerView.NO_ID : id;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        MemberResponse member = members.get(position);
        holder.bind(
                member,
                actionsEnabled,
                Objects.equals(statusChangingMemberId, member.getId()),
                historyClickListener,
                editClickListener,
                statusClickListener
        );
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static final class MemberViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView statusText;
        private final TextView cardNumberText;
        private final TextView emailText;
        private final TextView phoneText;
        private final ImageButton historyButton;
        private final ImageButton editButton;
        private final ImageButton statusButton;

        MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.memberNameText);
            statusText = itemView.findViewById(R.id.memberStatusText);
            cardNumberText = itemView.findViewById(R.id.memberCardNumberText);
            emailText = itemView.findViewById(R.id.memberEmailText);
            phoneText = itemView.findViewById(R.id.memberPhoneText);
            historyButton = itemView.findViewById(R.id.memberHistoryButton);
            editButton = itemView.findViewById(R.id.editMemberButton);
            statusButton = itemView.findViewById(R.id.changeMemberStatusButton);
        }

        void bind(
                MemberResponse member,
                boolean actionsEnabled,
                boolean statusChanging,
                OnMemberHistoryClickListener historyClickListener,
                OnMemberEditClickListener editClickListener,
                OnMemberStatusClickListener statusClickListener
        ) {
            String firstName = member.getFirstName() == null ? "" : member.getFirstName();
            String lastName = member.getLastName() == null ? "" : member.getLastName();
            String fullName = (firstName + " " + lastName).trim();
            if (fullName.isEmpty()) {
                fullName = itemView.getContext().getString(R.string.member_name_missing);
            }
            nameText.setText(fullName);

            boolean active = Boolean.TRUE.equals(member.getActive());
            statusText.setText(active
                    ? R.string.member_status_active
                    : R.string.member_status_inactive);

            String cardNumber = valueOrMissing(member.getMembershipCardNumber());
            cardNumberText.setText(itemView.getContext().getString(
                    R.string.member_card_number_value,
                    cardNumber
            ));

            String email = valueOrMissing(member.getEmail());
            emailText.setText(itemView.getContext().getString(
                    R.string.member_email_value,
                    email
            ));

            String phone = valueOrMissing(member.getPhone());
            phoneText.setText(itemView.getContext().getString(
                    R.string.member_phone_value,
                    phone
            ));

            historyButton.setContentDescription(itemView.getContext().getString(
                    R.string.member_history_content_description,
                    fullName
            ));
            boolean historyAvailable = historyClickListener != null;
            historyButton.setVisibility(historyAvailable ? View.VISIBLE : View.GONE);
            historyButton.setEnabled(actionsEnabled && historyAvailable);
            historyButton.setOnClickListener(historyAvailable
                    ? ignored -> historyClickListener.onHistoryClick(member)
                    : null);

            editButton.setContentDescription(itemView.getContext().getString(
                    R.string.edit_member_content_description,
                    fullName
            ));
            boolean editingAvailable = editClickListener != null;
            editButton.setVisibility(editingAvailable ? View.VISIBLE : View.GONE);
            editButton.setEnabled(actionsEnabled && editingAvailable);
            editButton.setOnClickListener(editingAvailable
                    ? ignored -> editClickListener.onEditClick(member)
                    : null);

            statusButton.setImageResource(active
                    ? R.drawable.ic_deactivate_member
                    : R.drawable.ic_activate_member);
            statusButton.setContentDescription(itemView.getContext().getString(
                    active
                            ? R.string.deactivate_member_content_description
                            : R.string.activate_member_content_description,
                    fullName
            ));
            statusButton.setEnabled(actionsEnabled && !statusChanging);
            statusButton.setOnClickListener(ignored -> statusClickListener.onStatusClick(member));
        }

        private String valueOrMissing(String value) {
            if (value == null || value.trim().isEmpty()) {
                return itemView.getContext().getString(R.string.member_value_missing);
            }
            return value;
        }
    }

    public interface OnMemberHistoryClickListener {
        void onHistoryClick(MemberResponse member);
    }

    public interface OnMemberEditClickListener {
        void onEditClick(MemberResponse member);
    }

    public interface OnMemberStatusClickListener {
        void onStatusClick(MemberResponse member);
    }
}
