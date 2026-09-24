package lib.widget

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File

data class FileNode(
    val file: File,
    val depth: Int,
    var isExpanded: Boolean = false
)

class FolderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var targetDirectory: File
    private var selectedFile: File? = null
    private val expandedPaths = mutableSetOf<String>()
    private var onFileSelected: ((File) -> Unit)? = null

    private val recyclerView: RecyclerView
    private val emptyView: TextView
    private val treeAdapter: TreeAdapter

    init {
        val fallbackDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        targetDirectory = fallbackDir

        recyclerView = RecyclerView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
        }

        emptyView = TextView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            gravity = Gravity.CENTER
            textSize = 14f
            setTextColor(context.getColor(lib.R.color.text))
            text = "Empty Directory"
            visibility = GONE
        }

        treeAdapter = TreeAdapter { clickedNode, position ->
            onNodeClicked(clickedNode, position)
        }
        recyclerView.adapter = treeAdapter
        addView(recyclerView)
        addView(emptyView)

        segarkan()
    }

    private fun onNodeClicked(node: FileNode, position: Int) {
        if (node.file.isDirectory) {
            treeAdapter.toggleFolder(node, position)
            if (node.isExpanded) expandedPaths.add(node.file.absolutePath)
            else expandedPaths.remove(node.file.absolutePath)
        } else {
            selectedFile = node.file
            treeAdapter.setSelectedPosition(position)
            onFileSelected?.invoke(node.file)
        }
    }

    fun segarkan() {
        val visible = mutableListOf<FileNode>()
        try {
            val files = targetDirectory.listFiles()?.toList() ?: emptyList()
            val sortedFiles = files.sortedWith(
                compareBy({ !it.isDirectory }, { it.name.lowercase() })
            )
            for (f in sortedFiles) {
                val node = FileNode(f, 0, isExpanded = expandedPaths.contains(f.absolutePath))
                visible.add(node)
                if (node.isExpanded && f.isDirectory) restoreChildren(visible, node)
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        treeAdapter.setRootItems(visible)
        updateEmptyState()
    }

    fun setDir(terima: File) {
        if (terima.exists() && terima.isDirectory) {
            targetDirectory = terima
            expandedPaths.clear()
            selectedFile = null
            treeAdapter.clearSelection()
            segarkan()
        }
    }

    fun getFile(): File = selectedFile ?: targetDirectory
    fun getSelectedFile(): File? = selectedFile
    fun getCurrentDir(): File = targetDirectory

    fun clearSelection() {
        selectedFile = null
        treeAdapter.clearSelection()
    }

    fun setOnFileSelectedListener(listener: (File) -> Unit) {
        onFileSelected = listener
    }

    fun scrollToPath(path: String) {
        val idx = treeAdapter.indexOfPath(path)
        if (idx >= 0) recyclerView.scrollToPosition(idx)
    }

    private fun restoreChildren(visible: MutableList<FileNode>, parent: FileNode) {
        val children = try {
            parent.file.listFiles()?.toList() ?: emptyList()
        } catch (e: SecurityException) { emptyList() }
        val sorted = children.sortedWith(
            compareBy({ !it.isDirectory }, { it.name.lowercase() })
        )
        for (f in sorted) {
            val node = FileNode(f, parent.depth + 1, isExpanded = expandedPaths.contains(f.absolutePath))
            visible.add(node)
            if (node.isExpanded && f.isDirectory) restoreChildren(visible, node)
        }
    }

    private fun updateEmptyState() {
        val empty = treeAdapter.itemCount == 0
        emptyView.visibility = if (empty) VISIBLE else GONE
        recyclerView.visibility = if (empty) GONE else VISIBLE
    }

    private fun dpToPx(context: Context, dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }

    private inner class TreeAdapter(
        private val onItemClick: (FileNode, Int) -> Unit
    ) : RecyclerView.Adapter<TreeAdapter.TreeViewHolder>() {

        private val visibleNodes: MutableList<FileNode> = mutableListOf()
        private var selectedPosition: Int = RecyclerView.NO_POSITION

        fun setRootItems(newNodes: List<FileNode>) {
            visibleNodes.clear()
            visibleNodes.addAll(newNodes)
            selectedPosition = RecyclerView.NO_POSITION
            notifyDataSetChanged()
        }

        fun setSelectedPosition(position: Int) {
            val old = selectedPosition
            selectedPosition = position
            if (old != RecyclerView.NO_POSITION && old < visibleNodes.size) notifyItemChanged(old)
            if (position != RecyclerView.NO_POSITION && position < visibleNodes.size) notifyItemChanged(position)
        }

        fun clearSelection() = setSelectedPosition(RecyclerView.NO_POSITION)

        fun indexOfPath(path: String): Int =
            visibleNodes.indexOfFirst { it.file.absolutePath == path }

        fun toggleFolder(node: FileNode, position: Int) {
            if (position == RecyclerView.NO_POSITION || position >= visibleNodes.size) return

            if (node.isExpanded) {
                var removeCount = 0
                while (position + 1 < visibleNodes.size &&
                    visibleNodes[position + 1].depth > node.depth
                ) {
                    visibleNodes.removeAt(position + 1)
                    removeCount++
                }
                node.isExpanded = false
                if (removeCount > 0) notifyItemRangeRemoved(position + 1, removeCount)
                notifyItemChanged(position)
            } else {
                val children = try {
                    node.file.listFiles()?.toList() ?: emptyList()
                } catch (e: SecurityException) { emptyList() }
                val sortedChildren = children.sortedWith(
                    compareBy({ !it.isDirectory }, { it.name.lowercase() })
                )
                val childNodes = sortedChildren.map {
                    FileNode(it, node.depth + 1, isExpanded = expandedPaths.contains(it.absolutePath))
                }
                visibleNodes.addAll(position + 1, childNodes)
                node.isExpanded = true
                if (childNodes.isNotEmpty()) notifyItemRangeInserted(position + 1, childNodes.size)
                notifyItemChanged(position)
            }
            updateEmptyState()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TreeViewHolder {
            val itemContext = parent.context

            val container = LinearLayout(itemContext).apply {
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                isClickable = true
                isFocusable = true

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val tv = TypedValue()
                    if (itemContext.theme.resolveAttribute(
                            android.R.attr.selectableItemBackground, tv, true
                        ) && tv.resourceId != 0
                    ) {
                        foreground = itemContext.getDrawable(tv.resourceId)
                    }
                }
            }

            val indicatorView = TextView(itemContext).apply {
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(itemContext, 20f),
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                textSize = 10f
                gravity = Gravity.CENTER
                setTextColor(context.getColor(lib.R.color.text))
            }

            val iconView = ImageView(itemContext).apply {
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(itemContext, 18f),
                    dpToPx(itemContext, 18f)
                ).apply { marginEnd = dpToPx(itemContext, 8f) }
            }

            val textView = TextView(itemContext).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
                textSize = 14f
                setTextColor(context.getColor(lib.R.color.text))
                isSingleLine = true
                ellipsize = TextUtils.TruncateAt.MIDDLE
            }

            container.addView(indicatorView)
            container.addView(iconView)
            container.addView(textView)

            return TreeViewHolder(container, indicatorView, iconView, textView)
        }

        override fun onBindViewHolder(holder: TreeViewHolder, position: Int) {
            val node = visibleNodes[position]
            holder.bind(node, position == selectedPosition)
            holder.itemView.setOnClickListener {
                @Suppress("DEPRECATION")
                val currentPos = holder.adapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    onItemClick(node, currentPos)
                }
            }
        }

        override fun getItemCount(): Int = visibleNodes.size

        inner class TreeViewHolder(
            view: android.view.View,
            private val indicator: TextView,
            private val icon: ImageView,
            private val title: TextView
        ) : RecyclerView.ViewHolder(view) {

            private val hostActivity: Activity? = resolveActivity(view.context)

            fun bind(node: FileNode, isSelected: Boolean) {
                title.text = node.file.name

                val basePadding = dpToPx(itemView.context, 8f)
                val indent = dpToPx(itemView.context, (node.depth * 20).toFloat())
                itemView.setPadding(indent, basePadding, basePadding, basePadding)

                if (node.file.isDirectory) {
                    Glide.with(icon).clear(icon)
                    icon.setImageResource(lib.R.drawable.folder)
                    indicator.text = if (node.isExpanded) "▼" else "▶"
                    title.setTypeface(null, Typeface.BOLD)
                } else {
                    val act = hostActivity
                    if (act != null) {
                        val ikon = lib.view.IconFile(act)
                        ikon.setGlide(icon)
                        icon.setImageResource(ikon.Type(node.file))
                    }
                    indicator.text = ""
                    title.setTypeface(null, Typeface.NORMAL)
                }

                itemView.setBackgroundColor(
                    if (isSelected) context.getColor(lib.R.color.parent) else Color.TRANSPARENT
                )
            }
        }
    }

    companion object {
        private fun resolveActivity(ctx: Context): Activity? {
            var c: Context? = ctx
            while (c is ContextWrapper) {
                if (c is Activity) return c
                c = c.baseContext
            }
            return null
        }
    }
}