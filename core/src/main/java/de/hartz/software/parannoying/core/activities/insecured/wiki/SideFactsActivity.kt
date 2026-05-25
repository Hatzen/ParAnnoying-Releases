package de.hartz.software.parannoying.core.activities.insecured.wiki

import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction
import com.yuyakaido.android.cardstackview.Duration
import com.yuyakaido.android.cardstackview.StackFrom
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting
import com.yuyakaido.android.cardstackview.SwipeableMethod
import de.hartz.software.parannoying.core.R
import de.hartz.software.parannoying.core.adapter.SideFactCardAdapter
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.core.interfaces.di.ApplicationInfoComponent
import de.hartz.software.parannoying.core.model.domain.SideFact
import de.hartz.software.parannoying.core.model.exceptions.SideFactFeedbackExceptionWrapper
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import org.acra.ACRA
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SideFactsActivity : AppCompatActivity() {

    @Inject
    lateinit var applicationInfoComponent: ApplicationInfoComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_side_facts)

        app.coreComponents.inject(this)

        val cardStackView = findViewById<CardStackView>(R.id.card_stack_view)
        val setting = SwipeAnimationSetting.Builder()
            .setDirection(Direction.Right)
            .setDuration(Duration.Normal.duration)
            .setInterpolator(AccelerateInterpolator())
            .build()


        val facts = loadData().shuffled()
        val adapter =  SideFactCardAdapter(facts)
        cardStackView.adapter = adapter
        val konfettiView = findViewById<KonfettiView>(R.id.konfettiView)

        val explode = Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
            position = Position.Relative(0.5, 0.5)
        )

        konfettiView.start(explode)

        val listener = object: CardStackListener {
            override fun onCardDragging(direction: Direction?, ratio: Float) {}
            override fun onCardSwiped(direction: Direction?) {
                val currentIndex = (cardStackView.layoutManager as CardStackLayoutManager).topPosition
                if (currentIndex >= adapter.sideFacts.size) {
                    // TODO: add archivements
                    return
                }
                val currentItem = adapter.sideFacts[currentIndex]
                when (direction) {
                    Direction.Left -> {
                        val itemNumber = currentItem.id
                        val title = "Provide a feedback for fixing #$itemNumber (NOT ENCRYPTED)"
                        val freeText = true
                        val callback = object: DialogHelper.InputDialogCallback() {
                            override fun onFinish(text: String) {
                                val artificialException = SideFactFeedbackExceptionWrapper(text, currentItem, applicationInfoComponent)
                                if (app.Storage.isOfflineDevice()) {
                                    throw artificialException
                                } else {
                                    ACRA.errorReporter.handleSilentException(artificialException)
                                }
                            }
                        }
                        DialogHelper.showInputDialog(this@SideFactsActivity, title, freeText, callback)
                    }
                    Direction.Right -> {
                        if (currentItem.isFact) {
                            konfettiView.start(explode)
                        }
                    }
                    Direction.Top -> {}
                    Direction.Bottom -> {}
                    null -> {}
                }
            }
            override fun onCardRewound() {}
            override fun onCardCanceled() {}
            override fun onCardAppeared(view: View?, position: Int) {}
            override fun onCardDisappeared(view: View?, position: Int) {}

        }

        val layoutManager = CardStackLayoutManager(applicationContext, listener)
        layoutManager.setSwipeAnimationSetting(setting)
        layoutManager.setStackFrom(StackFrom.Top)
        layoutManager.setVisibleCount(20)
        layoutManager.setMaxDegree(20.0f)
        layoutManager.setSwipeableMethod(SwipeableMethod.Manual)
        layoutManager.topPosition


        cardStackView.layoutManager = layoutManager
        cardStackView.adapter = SideFactCardAdapter(facts)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp() : Boolean {
        // Enable Back button.
        finish()
        return true
    }

    private fun loadData(): List<SideFact> {
        val resultList = mutableListOf<SideFact>()
        var id = 1
        val path = "sidefacts"
        val categoryFolders = assets.list(path)!!

        for (category in categoryFolders) {
            val facts = assets.list("$path/$category")!!
            for (fact in facts) {
                val file = assets.open("$path/$category/$fact")
                val content = getTextFromFile(file)
                resultList.add(SideFact("#${id++}", category, content, true))
            }
        }
        return resultList
    }

    private fun getTextFromFile(file: InputStream): String {
        val stringBuilder = StringBuilder()
        val input = BufferedReader(InputStreamReader(file, "UTF-8"))

        var line: String? = null
        while ({ line = input.readLine(); line }() != null) {
            stringBuilder.append(line)
        }
        input.close()
        return stringBuilder.toString()
    }
}