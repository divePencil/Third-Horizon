package com.freediving.dto;

import com.freediving.domain.ActivityExpense;
import com.freediving.domain.ActivityExpenseShare;
import java.util.List;

public record ActivityExpenseResponse(ActivityExpense expense, List<ActivityExpenseShare> shares) {
}
