package de.hartz.software.parannoying.core.extensions

import androidx.fragment.app.Fragment
import de.hartz.software.parannoying.core.interfaces.AbstractApp
import de.hartz.software.parannoying.core.interfaces.di.StorageInterface

val Fragment.app: AbstractApp get() = requireActivity().application as AbstractApp

var Fragment.Storage: StorageInterface<*, *>
    get() = app.Storage
    set(value) { /* do something */ }
