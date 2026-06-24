package com.freediving.dto;

import com.freediving.domain.JoinGroup;
import com.freediving.domain.JoinSetting;
import java.util.List;

public record JoinInfoResponse(
        JoinSetting setting,
        List<JoinGroup> groups
) {
}
