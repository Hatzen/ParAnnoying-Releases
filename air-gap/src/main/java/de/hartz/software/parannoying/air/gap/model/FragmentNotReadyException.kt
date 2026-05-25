package de.hartz.software.parannoying.air.gap.model

class FragmentNotReadyException : IllegalStateException("Tried to switch to fast. Androids fade fragment animation would crash app.")