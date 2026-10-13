package com.example.model

data class AuditorProfile(
    val id: String,
    val name: String,
    val nip: String,
    val role: String,
    val email: String,
    val avatarInitials: String,
    val avatarColorHex: Long,
    val pin: String = "1234"
) {
    companion object {
        val DEFAULT_AUDITORS = listOf(
            AuditorProfile(
                id = "AUD_01",
                name = "Ahmad Fauzi, CFE",
                nip = "19880412-201502-1-002",
                role = "Lead Forensic Auditor",
                email = "ahmad.fauzi@audit-forensik.id",
                avatarInitials = "AF",
                avatarColorHex = 0xFF006A6A,
                pin = "1234"
            ),
            AuditorProfile(
                id = "AUD_02",
                name = "Siti Rahmawati, CIA",
                nip = "19920915-201801-2-004",
                role = "Senior Compliance Officer",
                email = "siti.rahma@audit-forensik.id",
                avatarInitials = "SR",
                avatarColorHex = 0xFF4A6267,
                pin = "1234"
            ),
            AuditorProfile(
                id = "AUD_03",
                name = "Budi Pratama, CAMS",
                nip = "19950320-202003-1-008",
                role = "Anti-Fraud Specialist",
                email = "budi.pratama@audit-forensik.id",
                avatarInitials = "BP",
                avatarColorHex = 0xFF8A5100,
                pin = "1234"
            )
        )
    }
}
