package com.freediving.dto;

import com.freediving.domain.ActivityOption;
import com.freediving.domain.ActivityPublishSetting;
import java.util.List;

public record ActivityPublishConfigResponse(
        ActivityPublishSetting setting,
        List<ActivityOption> options
) {
}
