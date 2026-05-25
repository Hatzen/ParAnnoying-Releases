package de.hartz.software.parannoying.core.model.domain

data class OnboardingSteps(var step: Int = UNKNOWN_VAL) {
    companion object {
        private const val UNKNOWN_VAL: Int = -1
        private const val CHAT_OVERVIEW_VAL: Int = 0
        private const val CHAT_VAL: Int = 1
        private const val BARCODE_MESSAGE_VAL: Int = 2
        private const val BARCODE_USERID_VAL: Int = 3
        private const val BARCODE_NOTIFICATIONID_VAL: Int = 4
        private const val BARCODE_MESSAGE_OVERVIEW_VAL: Int = 5

        val UNKNOWN = OnboardingSteps(UNKNOWN_VAL)
        val CHAT_OVERVIEW = OnboardingSteps(CHAT_OVERVIEW_VAL)
        val CHAT = OnboardingSteps(CHAT_VAL)
        val BARCODE_MESSAGE = OnboardingSteps(BARCODE_MESSAGE_VAL)
        val BARCODE_USERID = OnboardingSteps(BARCODE_USERID_VAL)
        val BARCODE_NOTIFICATIONID = OnboardingSteps(BARCODE_NOTIFICATIONID_VAL)
        val BARCODE_MESSAGE_OVERVIEW = OnboardingSteps(BARCODE_MESSAGE_OVERVIEW_VAL)

        fun getByChannel(channel: Int) : OnboardingSteps? {
            val listOfConsts = listOf(UNKNOWN, CHAT_OVERVIEW, CHAT, BARCODE_MESSAGE, BARCODE_USERID, BARCODE_NOTIFICATIONID, BARCODE_MESSAGE_OVERVIEW)
            for(channelO in listOfConsts) {
                if (channelO.step == channel) {
                    return  channelO
                }
            }
            return null
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is OnboardingSteps) {
            return step == other.step
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return step
    }

}
