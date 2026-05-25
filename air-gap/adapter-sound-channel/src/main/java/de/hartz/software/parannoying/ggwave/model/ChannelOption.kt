package de.hartz.software.parannoying.ggwave.model

enum class ChannelOption(val value: Int) {
    GGWAVE_TX_PROTOCOL_AUDIBLE_NORMAL(0),
    GGWAVE_TX_PROTOCOL_AUDIBLE_FAST(1),
    GGWAVE_TX_PROTOCOL_AUDIBLE_FASTEST(2),
    GGWAVE_TX_PROTOCOL_ULTRASOUND_NORMAL(3),
    GGWAVE_TX_PROTOCOL_ULTRASOUND_FAST(4),
    GGWAVE_TX_PROTOCOL_ULTRASOUND_FASTEST(5),
    GGWAVE_TX_PROTOCOL_DT_NORMAL(6),
    GGWAVE_TX_PROTOCOL_DT_FAST(7),
    GGWAVE_TX_PROTOCOL_DT_FASTEST(8);

    companion object {
        fun getByValue(value: Int): ChannelOption {
            return ChannelOption.values().find { it.value == value }!!
        }
    }
}