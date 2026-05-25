package de.hartz.software.parannoying.core.activities.insecured.wiki

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.TextAppearanceSpan
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.sysdata.widget.accordion.CollapsedViewHolder
import com.sysdata.widget.accordion.ExpandableItemHolder
import com.sysdata.widget.accordion.ExpandedViewHolder
import com.sysdata.widget.accordion.FancyAccordionView
import com.sysdata.widget.accordion.Item
import com.sysdata.widget.accordion.ItemAdapter
import de.hartz.software.parannoying.core.R
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class AboutActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    companion object {
        var currentQuery = ""
    }

    private lateinit var mRecyclerView : FancyAccordionView
    private lateinit var allWikiEntries: List<WikiEntryModel>

    private val mListener = ItemAdapter.OnItemClickedListener { viewHolder, id ->
        /*val itemHolder = viewHolder.itemHolder
        val item = itemHolder.item

        when (id) {
            //ItemAdapter.OnItemClickedListener.ACTION_ID_COLLAPSED_VIEW -> Toast.makeText(AboutActivity.this, String.format("Collapsed " + item.getTitle() + " clicked!", Toast.LENGTH_LONG)).show()
            //ItemAdapter.OnItemClickedListener.ACTION_ID_EXPANDED_VIEW -> Toast.makeText(String.format("Expanded %s clicked!", item.getTitle()))
            else -> {}
        }// do nothing*/

        // (viewHolder as TextMarkable).markText("crash")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setSupportActionBar(findViewById(R.id.toolbar))

        title = "About ParAnnoying"

        mRecyclerView = findViewById(R.id.fancy_accordion_view)
        mRecyclerView.setCollapsedViewHolderFactory(WikiEntryCollapsedViewHolder.Factory.create(R.layout.cell_about), mListener)
        mRecyclerView.setExpandedViewHolderFactory(WikiEntryExpandedViewHolder.Factory.create(R.layout.cell_about_expanded), mListener)
        allWikiEntries = loadData()
        createViewHolders(allWikiEntries)

    }

    override fun onCreateOptionsMenu(menu: Menu):Boolean {
        getMenuInflater().inflate(R.menu.menu_wiki, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.getActionView() as SearchView
        searchView.setOnQueryTextListener(this)
        return true
    }

    override fun onQueryTextChange(query: String): Boolean {
        val filteredModelList = getFilteredList(allWikiEntries, query)
        createViewHolders(filteredModelList)
        mRecyclerView.scrollToPosition(0)
        markAllMatches(query)
        currentQuery = query
        return true
    }

    override fun onQueryTextSubmit(query:String):Boolean {
        return false
    }

    private fun createViewHolders (itemList: List<WikiEntryModel>) {
        val itemHolders = ArrayList<ExpandableItemHolder<Item>>()
        for (wikiEntry in itemList) {
            val itemHolder = ExpandableItemHolder(wikiEntry as Item)
            itemHolders.add(itemHolder)
        }
        mRecyclerView.setAdapterItems(itemHolders)
    }

    private fun loadData(): List<WikiEntryModel> {
        val resultList = mutableListOf<WikiEntryModel>()
        val path = "aboutTexts"
        val list = assets.list(path)!!

        for (fileName in list) {
            val file = assets.open("$path/$fileName")
            val name = fileName.replace('_', ' ')
            val content = getTextFromFile(file)
            resultList.add(WikiEntryModel.create(name, content))
        }
        return resultList
    }

    private fun getTextFromFile(file: InputStream): String {
        val stringBuilder = StringBuilder()
        val input = BufferedReader(InputStreamReader(file, "UTF-8"))

        var line: String? = null
        while ({ line = input.readLine(); line }() != null) {
            stringBuilder.append(line + "<br>")
        }
        input.close()
        return stringBuilder.toString().replace("- ", "• ")
    }

    private fun getFilteredList(allWikiEntries: List<WikiEntryModel>, query: String ): List<WikiEntryModel>  {
        val notMatching: MutableList<WikiEntryModel> = allWikiEntries.toMutableList()
        val matching: MutableList<WikiEntryModel> = mutableListOf()

        val comparableQuery = query.toLowerCase()

        // Get all perfectly matching
        var iterator = notMatching.iterator()
        while (iterator.hasNext()) {
            val it = iterator.next()
            if (it.title.toLowerCase().contains(comparableQuery) || it.description!!.toLowerCase().contains(comparableQuery)) {
                matching.add(it)
                iterator.remove()
            }
        }

        // TODO: Only if query bigger than 5?

        // Get all partitally matching
        iterator = notMatching.iterator()
        while (iterator.hasNext()) {
            val it = iterator.next()
            val parts = comparableQuery.split(" ")
            for (part in parts) {
                if (it.title.toLowerCase().contains(part) || it.description!!.toLowerCase().contains(part)) {
                    matching.add(it)
                    iterator.remove()
                    break
                }
            }
        }

        iterator = notMatching.iterator()
        while (iterator.hasNext()) {
            val it = iterator.next()
            val parts = query.split(" ")
            for (part in parts) {
                if (getResultsWithTypo(it.title.toLowerCase(), comparableQuery)
                    || getResultsWithTypo(it.description!!.toLowerCase(), comparableQuery)) {
                    matching.add(it)
                    iterator.remove()
                    break
                }
            }
        }
        return matching
    }

    private fun getResultsWithTypo (content: String, query: String): Boolean {
        if (query.isEmpty()) {
            return false
        }
        content.withIndex()
            .filter {it.value == query[0]} // Contains first letter cause probably it is not a typo.
            .forEach {
                var matches = 0
                val range = content.length / 2 - 2
                val startIndex = it.index
                val start = Math.max(startIndex - range, 0)
                val end = Math.min(startIndex + range, content.length - 1)
                var neighbourhood = content.subSequence(start, end)
                for (char in query.toCharArray().distinct()) {
                    val contentPart = neighbourhood.indexOf(char)
                    if (contentPart != -1) {
                        matches++
                        neighbourhood = neighbourhood.removeRange(contentPart, contentPart + 1) // If x is matched once it should not force more matches.
                    }
                }
                if (matches > 0.9 * query.length) {
                    return true
                }
            }
        return false
    }

    private fun markAllMatches(query: String) {
        for (i in 0 until mRecyclerView.childCount) {
            val item3 = mRecyclerView.getChildViewHolder(mRecyclerView.getChildAt(i)) as TextMarkable
            item3.markText(query)
        }
    }
}

interface TextMarkable {
    fun markText(query: String)

    fun markElement(target: TextView, query: String) {
        val wholeText = target.text.toString()
        val searchableText = target.text.toString().toLowerCase()
        val comparableQuery = query.toLowerCase()

        val str = SpannableString(wholeText)
        if (query.isNotEmpty()) {
            searchableText.withIndex()
                .filter {it.value == query[0]}
                .forEach {
                    val index = searchableText.indexOf(comparableQuery, it.index)
                    if (index != -1) {
                        str.setSpan(BackgroundColorSpan(Color.YELLOW), index, index + query.length, 0)
                    } else if (false) {
                        // TODO: Do other styling..
                        str.setSpan(TextAppearanceSpan("Arial", 0, 16,
                            ColorStateList.valueOf(Color.LTGRAY), ColorStateList.valueOf(Color.BLUE)), index, query.length, 0)
                    }
                }
        }
        target.setText(str)
    }
}

class WikiEntryCollapsedViewHolder private constructor(itemView: View) : CollapsedViewHolder(itemView),
    TextMarkable {
    override fun markText(query: String) {
        markElement(mTitleTextView, query)
    }

    private val mTitleTextView: TextView

    init {
        mTitleTextView = itemView.findViewById(R.id.sample_layout_collapsed_title)
    }

    override fun onBindItemView(itemHolder: ExpandableItemHolder<*>) {
        mTitleTextView.setText((itemHolder.item as WikiEntryModel).title)
        markElement(mTitleTextView,
            AboutActivity.currentQuery
        )
    }

    override fun onRecycleItemView() {
    }

    override fun getViewHolderFactory(): ItemAdapter.ItemViewHolder.Factory? {
        return null
    }

    class Factory internal constructor(@param:LayoutRes @field:LayoutRes
                                       private val mItemViewLayoutId: Int) : ItemAdapter.ItemViewHolder.Factory {

        override fun createViewHolder(parent: ViewGroup, viewType: Int): ItemAdapter.ItemViewHolder<*> {
            val itemView = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
            return WikiEntryCollapsedViewHolder(itemView)
        }

        override fun getItemViewLayoutId(): Int {
            return mItemViewLayoutId
        }

        companion object {

            fun create(@LayoutRes itemViewLayoutId: Int): Factory {
                return Factory(itemViewLayoutId)
            }
        }
    }
}

class WikiEntryExpandedViewHolder private constructor(itemView: View) : ExpandedViewHolder(itemView),
    TextMarkable {
    override fun markText(query: String) {
        markElement(mTitleTextView,
            query)
        markElement(mDescriptionTextView,
            query)
    }

    private val mTitleTextView: TextView = itemView.findViewById(R.id.sample_layout_expanded_title) as TextView
    private val mDescriptionTextView: TextView = itemView.findViewById(R.id.sample_layout_expanded_description) as TextView

    override fun onBindItemView(itemHolder: ExpandableItemHolder<*>) {
        mTitleTextView.text = (itemHolder.item as WikiEntryModel).title
        val description = (itemHolder.item as WikiEntryModel).description
        mDescriptionTextView.autoLinkMask = Linkify.WEB_URLS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mDescriptionTextView.setText(Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY))
        } else {
            mDescriptionTextView.setText(Html.fromHtml(description))
        }

        markElement(mTitleTextView,
            AboutActivity.currentQuery
        )
        markElement(mDescriptionTextView,
            AboutActivity.currentQuery
        )
    }

    override fun onRecycleItemView() {
    }

    override fun getViewHolderFactory(): ItemAdapter.ItemViewHolder.Factory? {
        return null
    }

    class Factory(@param:LayoutRes @field:LayoutRes
                  private val mItemViewLayoutId: Int) : ItemAdapter.ItemViewHolder.Factory {

        override fun createViewHolder(parent: ViewGroup, viewType: Int): ItemAdapter.ItemViewHolder<*> {
            val itemView = LayoutInflater.from(parent.context).inflate(viewType, parent, false /* attachToRoot */)
            return WikiEntryExpandedViewHolder(itemView)
        }

        override fun getItemViewLayoutId(): Int {
            return mItemViewLayoutId
        }

        companion object {

            fun create(@LayoutRes itemViewLayoutId: Int): Factory {
                return Factory(itemViewLayoutId)
            }
        }
    }
}


class WikiEntryModel internal constructor(val title: String, val description: String?) : Item() {

    override fun getUniqueId(): Int {
        return hashCode()
    }

    override fun equals(o: Any?): Boolean {
        if (this === o)
            return true
        if (o == null || WikiEntryModel.javaClass !== o.javaClass)
            return false

        val that = o as WikiEntryModel?

        if (title != that!!.title)
            return false
        return if (description != null) description == that.description else that.description == null
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        return result
    }

    companion object {

        fun create(title: String, description: String): WikiEntryModel {
            return WikiEntryModel(title, description)
        }
    }
}