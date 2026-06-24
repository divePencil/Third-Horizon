package com.freediving.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ActivityExpenseRequest(
        @NotBlank String title,
        @Min(1) Integer amountCents,
        String note,
        String receiptUrl,
        @NotEmpty List<Long> shareUserIds
) {
}
