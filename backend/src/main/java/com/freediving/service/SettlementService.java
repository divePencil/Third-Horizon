package com.freediving.service;

import com.freediving.domain.ActivityExpense;
import com.freediving.domain.ActivityExpenseShare;
import com.freediving.domain.ActivityMember;
import com.freediving.domain.ActivityMemberRole;
import com.freediving.domain.ActivityMemberStatus;
import com.freediving.domain.ActivitySettlement;
import com.freediving.domain.ActivitySettlementItem;
import com.freediving.dto.SettlementResponse;
import com.freediving.repository.ActivityExpenseRepository;
import com.freediving.repository.ActivityExpenseShareRepository;
import com.freediving.repository.ActivityMemberRepository;
import com.freediving.repository.ActivitySettlementItemRepository;
import com.freediving.repository.ActivitySettlementRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class SettlementService {

    private final ActivityMemberRepository memberRepository;
    private final ActivityExpenseRepository expenseRepository;
    private final ActivityExpenseShareRepository shareRepository;
    private final ActivitySettlementRepository settlementRepository;
    private final ActivitySettlementItemRepository settlementItemRepository;

    public SettlementService(
            ActivityMemberRepository memberRepository,
            ActivityExpenseRepository expenseRepository,
            ActivityExpenseShareRepository shareRepository,
            ActivitySettlementRepository settlementRepository,
            ActivitySettlementItemRepository settlementItemRepository
    ) {
        this.memberRepository = memberRepository;
        this.expenseRepository = expenseRepository;
        this.shareRepository = shareRepository;
        this.settlementRepository = settlementRepository;
        this.settlementItemRepository = settlementItemRepository;
    }

    @Transactional
    public SettlementResponse generate(Long activityId, Long operatorUserId) {
        ActivityMember operator = memberRepository.findByActivityIdAndUserId(activityId, operatorUserId)
                .orElseThrow(() -> new EntityNotFoundException("activity member not found"));
        if (operator.getRole() != ActivityMemberRole.OWNER) {
            throw new IllegalArgumentException("只有活动发起人可以生成结算方案");
        }

        List<ActivityExpense> expenses = expenseRepository.findByActivityIdOrderByCreatedAtAsc(activityId);
        Map<Long, Integer> paid = new HashMap<>();
        Map<Long, Integer> owed = new HashMap<>();
        int total = 0;

        for (ActivityExpense expense : expenses) {
            total += expense.getAmountCents();
            paid.merge(expense.getPayerUserId(), expense.getAmountCents(), Integer::sum);
            for (ActivityExpenseShare share : shareRepository.findByExpenseId(expense.getId())) {
                owed.merge(share.getUserId(), share.getShareCents(), Integer::sum);
            }
        }

        List<ActivityMember> members = memberRepository.findByActivityIdAndStatusOrderByCreatedAtAsc(activityId, ActivityMemberStatus.JOINED);
        Map<Long, Integer> balance = new HashMap<>();
        for (ActivityMember member : members) {
            Long userId = member.getUserId();
            balance.put(userId, paid.getOrDefault(userId, 0) - owed.getOrDefault(userId, 0));
        }

        ActivitySettlement settlement = new ActivitySettlement();
        settlement.setActivityId(activityId);
        settlement.setCreatedBy(operatorUserId);
        settlement.setTotalAmountCents(total);
        ActivitySettlement savedSettlement = settlementRepository.save(settlement);

        List<BalanceLine> creditors = balance.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> new BalanceLine(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(BalanceLine::amount).reversed())
                .toList();
        List<BalanceLine> debtors = balance.entrySet().stream()
                .filter(entry -> entry.getValue() < 0)
                .map(entry -> new BalanceLine(entry.getKey(), -entry.getValue()))
                .sorted(Comparator.comparing(BalanceLine::amount).reversed())
                .toList();

        List<ActivitySettlementItem> items = new ArrayList<>();
        int debtorIndex = 0;
        int creditorIndex = 0;
        while (debtorIndex < debtors.size() && creditorIndex < creditors.size()) {
            BalanceLine debtor = debtors.get(debtorIndex);
            BalanceLine creditor = creditors.get(creditorIndex);
            int amount = Math.min(debtor.amount(), creditor.amount());
            if (amount > 0) {
                ActivitySettlementItem item = new ActivitySettlementItem();
                item.setSettlementId(savedSettlement.getId());
                item.setFromUserId(debtor.userId());
                item.setToUserId(creditor.userId());
                item.setAmountCents(amount);
                items.add(settlementItemRepository.save(item));
            }
            debtor = new BalanceLine(debtor.userId(), debtor.amount() - amount);
            creditor = new BalanceLine(creditor.userId(), creditor.amount() - amount);
            debtors.set(debtorIndex, debtor);
            creditors.set(creditorIndex, creditor);
            if (debtor.amount() == 0) {
                debtorIndex++;
            }
            if (creditor.amount() == 0) {
                creditorIndex++;
            }
        }

        return new SettlementResponse(savedSettlement, items);
    }

    public SettlementResponse latest(Long activityId) {
        return settlementRepository.findByActivityIdOrderByCreatedAtDesc(activityId).stream()
                .findFirst()
                .map(settlement -> new SettlementResponse(
                        settlement,
                        settlementItemRepository.findBySettlementIdOrderByIdAsc(settlement.getId())
                ))
                .orElse(null);
    }

    private record BalanceLine(Long userId, Integer amount) {
    }
}
