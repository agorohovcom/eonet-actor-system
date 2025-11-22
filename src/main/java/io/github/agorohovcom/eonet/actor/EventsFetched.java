package io.github.agorohovcom.eonet.actor;

import io.github.agorohovcom.eonet.model.EONETResponse;

public record EventsFetched(
        EONETResponse response
) {
}
