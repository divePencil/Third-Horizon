package com.freediving.dto;

import com.freediving.domain.ActivitySettlement;
import com.freediving.domain.ActivitySettlementItem;
import java.util.List;

public record SettlementResponse(ActivitySettlement settlement, List<ActivitySettlementItem> items) {
}
