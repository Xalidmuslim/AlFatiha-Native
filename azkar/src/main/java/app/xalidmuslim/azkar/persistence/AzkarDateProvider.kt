package app.xalidmuslim.azkar.persistence

import java.time.LocalDate
import java.time.ZoneId

fun interface AzkarDateProvider {
    fun currentDate(): LocalDate
}

object SystemAzkarDateProvider : AzkarDateProvider {
    override fun currentDate(): LocalDate = LocalDate.now(ZoneId.systemDefault())
}
