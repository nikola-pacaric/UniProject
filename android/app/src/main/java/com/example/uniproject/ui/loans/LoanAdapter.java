package com.example.uniproject.ui.loans;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uniproject.R;
import com.example.uniproject.data.model.loan.LoanResponse;
import com.example.uniproject.data.model.member.MemberResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class LoanAdapter extends RecyclerView.Adapter<LoanAdapter.LoanViewHolder> {
    private final OnLoanReturnClickListener returnClickListener;
    private List<LoanResponse> loans = Collections.emptyList();
    private Map<Long, String> memberNames = Collections.emptyMap();
    private boolean actionsEnabled = true;
    private Long returningLoanId;

    public LoanAdapter(OnLoanReturnClickListener returnClickListener) {
        this.returnClickListener = returnClickListener;
        setHasStableIds(true);
    }

    public void submitList(List<LoanResponse> loans) {
        this.loans = loans == null
                ? Collections.emptyList()
                : new ArrayList<>(loans);
        notifyDataSetChanged();
    }

    public void submitMembers(List<MemberResponse> members) {
        Map<Long, String> names = new HashMap<>();
        if (members != null) {
            for (MemberResponse member : members) {
                if (member.getId() == null) {
                    continue;
                }

                String firstName = member.getFirstName() == null
                        ? ""
                        : member.getFirstName();
                String lastName = member.getLastName() == null
                        ? ""
                        : member.getLastName();
                String fullName = (firstName + " " + lastName).trim();
                if (!fullName.isEmpty()) {
                    names.put(member.getId(), fullName);
                }
            }
        }
        memberNames = names;
        notifyDataSetChanged();
    }

    public void setActionsEnabled(boolean enabled) {
        if (actionsEnabled == enabled) {
            return;
        }
        actionsEnabled = enabled;
        notifyDataSetChanged();
    }

    public void setReturningLoanId(Long loanId) {
        if (Objects.equals(returningLoanId, loanId)) {
            return;
        }
        returningLoanId = loanId;
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
                .inflate(R.layout.item_loan, parent, false);
        return new LoanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LoanViewHolder holder, int position) {
        LoanResponse loan = loans.get(position);
        holder.bind(
                loan,
                memberNames.get(loan.getMemberId()),
                actionsEnabled,
                returningLoanId,
                returnClickListener
        );
    }

    @Override
    public int getItemCount() {
        return loans.size();
    }

    static final class LoanViewHolder extends RecyclerView.ViewHolder {
        private final TextView bookTitleText;
        private final TextView statusText;
        private final TextView memberText;
        private final TextView loanDateText;
        private final TextView dueDateText;
        private final TextView returnDateText;
        private final MaterialButton returnButton;

        LoanViewHolder(@NonNull View itemView) {
            super(itemView);
            bookTitleText = itemView.findViewById(R.id.loanBookTitleText);
            statusText = itemView.findViewById(R.id.loanStatusText);
            memberText = itemView.findViewById(R.id.loanMemberText);
            loanDateText = itemView.findViewById(R.id.loanDateText);
            dueDateText = itemView.findViewById(R.id.loanDueDateText);
            returnDateText = itemView.findViewById(R.id.loanReturnDateText);
            returnButton = itemView.findViewById(R.id.returnLoanButton);
        }

        void bind(
                LoanResponse loan,
                String memberName,
                boolean actionsEnabled,
                Long returningLoanId,
                OnLoanReturnClickListener returnClickListener
        ) {
            String bookTitle = loan.getBookTitleAtLoan();
            if (bookTitle == null || bookTitle.trim().isEmpty()) {
                bookTitle = itemView.getContext().getString(R.string.loan_book_missing);
            }
            bookTitleText.setText(bookTitle);

            String displayedMember = memberName;
            if (displayedMember == null || displayedMember.trim().isEmpty()) {
                displayedMember = loan.getMemberId() == null
                        ? itemView.getContext().getString(R.string.loan_member_missing)
                        : itemView.getContext().getString(
                                R.string.loan_member_unknown,
                                loan.getMemberId()
                        );
            }
            memberText.setText(itemView.getContext().getString(
                    R.string.loan_member_value,
                    displayedMember
            ));

            loanDateText.setText(itemView.getContext().getString(
                    R.string.loan_date_value,
                    formatDate(loan.getLoanDate())
            ));
            dueDateText.setText(itemView.getContext().getString(
                    R.string.loan_due_date_value,
                    formatDate(loan.getDueDate())
            ));

            String returnedDate = loan.getReturnDate();
            returnDateText.setText(itemView.getContext().getString(
                    R.string.loan_return_date_value,
                    returnedDate == null || returnedDate.trim().isEmpty()
                            ? itemView.getContext().getString(R.string.loan_not_returned)
                            : formatDate(returnedDate)
            ));

            boolean returned = isReturned(loan);
            renderStatus(loan, returned);

            boolean returnAvailable = !returned && returnClickListener != null;
            boolean returningThisLoan = Objects.equals(returningLoanId, loan.getId());
            returnButton.setVisibility(returnAvailable ? View.VISIBLE : View.GONE);
            returnButton.setText(returningThisLoan
                    ? R.string.return_loan_loading
                    : R.string.return_loan_action);
            returnButton.setContentDescription(itemView.getContext().getString(
                    R.string.return_loan_content_description,
                    bookTitle
            ));
            returnButton.setEnabled(
                    returnAvailable && actionsEnabled && returningLoanId == null
            );
            returnButton.setOnClickListener(returnAvailable
                    ? ignored -> returnClickListener.onReturnClick(loan)
                    : null);
        }

        private void renderStatus(LoanResponse loan, boolean returned) {
            boolean overdue = !returned && ("OVERDUE".equalsIgnoreCase(loan.getStatus())
                    || isPastDate(loan.getDueDate()));

            int statusLabel;
            int statusColorAttribute;
            if (returned) {
                statusLabel = R.string.loan_status_returned;
                statusColorAttribute = com.google.android.material.R.attr.colorOnSurfaceVariant;
            } else if (overdue) {
                statusLabel = R.string.loan_status_overdue;
                statusColorAttribute = com.google.android.material.R.attr.colorError;
            } else {
                statusLabel = R.string.loan_status_active;
                statusColorAttribute = com.google.android.material.R.attr.colorPrimary;
            }

            statusText.setText(statusLabel);
            statusText.setTextColor(MaterialColors.getColor(
                    statusText,
                    statusColorAttribute,
                    Color.BLACK
            ));
        }

        private boolean isReturned(LoanResponse loan) {
            return "RETURNED".equalsIgnoreCase(loan.getStatus())
                    || (loan.getReturnDate() != null
                    && !loan.getReturnDate().trim().isEmpty());
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
                return itemView.getContext().getString(R.string.loan_date_missing);
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

    public interface OnLoanReturnClickListener {
        void onReturnClick(LoanResponse loan);
    }
}
