package de.hartz.software.parannoying.core.model.exceptions

import de.hartz.software.parannoying.core.interfaces.di.ApplicationInfoComponent
import de.hartz.software.parannoying.core.model.domain.SideFact

class SideFactFeedbackExceptionWrapper(
    e: String, sideFact: SideFact, applicationInfoComponent: ApplicationInfoComponent)
    : FeedbackExceptionWrapper(getExceptionText(e, sideFact, applicationInfoComponent)) {

    companion object {
        private fun getExceptionText(
            e: String, sideFact: SideFact, applicationInfoComponent: ApplicationInfoComponent): String {
            return "Fix #${sideFact.id} within appversion ${applicationInfoComponent.getVersion()}, reason: $e"
        }
    }
}