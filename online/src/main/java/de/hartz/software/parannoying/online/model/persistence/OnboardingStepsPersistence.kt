package de.hartz.software.parannoying.online.model.persistence

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class OnboardingStepsPersistence : RealmObject() {
    @PrimaryKey var step: Int = 0

    override fun equals(other: Any?): Boolean {
        if (other is OnboardingStepsPersistence) {
            return step == other.step
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return step
    }

}
