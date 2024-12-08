package com.yucox.pillpulse.data.mapper

import com.yucox.pillpulse.data.remote.api.PillResponse
import com.yucox.pillpulse.domain.model.Pill
import org.mongodb.kbson.BsonObjectId

fun PillResponse.toDomain(): Pill {
    return Pill(
        _id = _id,
        id = id,
        drugName = drugName,
        whenYouTookHour = whenYouTookHour,
        whenYouTookDate = whenYouTookDate,
        userMail = userMail,
        month = month
    )
}

fun Pill.toResponse(): PillResponse {
    return PillResponse(
        _id = _id,
        id = id,
        drugName = drugName,
        whenYouTookHour = whenYouTookHour,
        whenYouTookDate = whenYouTookDate,
        userMail = userMail,
        month = month
    )
}