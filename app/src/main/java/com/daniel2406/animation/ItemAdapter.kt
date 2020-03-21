package com.daniel2406.animation

import android.animation.ValueAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_recycler.view.*

data class MainListModel(val id: Int)

var animationPlaybackSpeed: Double = 0.8

class ItemAdapter(context: Context) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    private val listItemHorizontalPadding: Float by bindDimen(
        context,
        R.dimen.list_item_horizontal_padding
    )
    private val listItemVerticalPadding: Float by bindDimen(
        context,
        R.dimen.list_item_vertical_padding
    )

    private var originalHeight = -1
    private var expandedHeight = -1

    private val originalWidth = context.screenWidth - 48.dp
    private val expandedWidth = context.screenWidth - 24.dp


    private val modelList = List(20) { MainListModel(it) }

    private var expandedModel: MainListModel? = null
    private var isScaledDown = false

    private val listItemExpandDuration: Long get() = (300L / animationPlaybackSpeed).toLong()

    private lateinit var recyclerView: RecyclerView


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        ItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_recycler, parent, false)
        )

    override fun getItemCount(): Int = modelList.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val model = modelList[position]
        expandItem(model == expandedModel, false, holder)
        scaleDownItem(holder, position, isScaledDown)

        holder.itemView.card_container.setOnClickListener {
            if (expandedModel == null) {
                expandItem(animate = true, expand = true, holder = holder)
                expandedModel = model
            } else if (expandedModel == model) {

                expandItem(expand = false, animate = true, holder = holder)
                expandedModel = null
            } else {

                val expandedModelPosition = modelList.indexOf(expandedModel!!)
                val oldViewHolder =
                    recyclerView.findViewHolderForAdapterPosition(expandedModelPosition) as? ItemViewHolder
                if (oldViewHolder != null) expandItem(
                    expand = false,
                    animate = true,
                    holder = oldViewHolder
                )

                expandItem(expand = true, animate = true, holder = holder)
                expandedModel = model
            }
        }
    }


    private fun expandItem(animate: Boolean, expand: Boolean, holder: ItemViewHolder) {
        if (animate) {
            val animator = getValueAnimator(
                expand, listItemExpandDuration, AccelerateDecelerateInterpolator()
            ) { progress -> setExpandProgress(holder, progress) }

            if (expand) animator.doOnStart { holder.itemView.expand_view.isVisible = true }
            else animator.doOnEnd { holder.itemView.expand_view.isVisible = false }

            animator.start()

        } else {
            holder.itemView.expand_view.isVisible = expand && expandedHeight >= 0
            setExpandProgress(holder, if (expand) 1f else 0f)
        }

    }

    private fun setExpandProgress(holder: ItemViewHolder, progress: Float) {
        if (expandedHeight > 0 && originalHeight > 0) {
            holder.itemView.card_container.layoutParams.height =
                (originalHeight + (expandedHeight - originalHeight) * progress).toInt()
        }

        holder.itemView.card_container.layoutParams.width =
            (originalWidth + (expandedWidth - originalWidth) * progress).toInt()


        holder.itemView.card_container.requestLayout()

        holder.itemView.chevron.rotation = 90 * progress
    }

    private fun setScaleDownProgress(holder: ItemViewHolder, position: Int, progress: Float) {
        val itemExpanded = position >= 0 && modelList[position] == expandedModel
        holder.itemView.card_container.layoutParams.apply {
            width =
                ((if (itemExpanded) expandedWidth else originalWidth) * (1 - 0.1f * progress)).toInt()
            height =
                ((if (itemExpanded) expandedHeight else originalHeight) * (1 - 0.1f * progress)).toInt()
        }
        holder.itemView.card_container.requestLayout()

        holder.itemView.card_container.scaleX = 1 - 0.05f * progress
        holder.itemView.card_container.scaleY = 1 - 0.05f * progress

        holder.itemView.card_container.setPadding(
            (listItemHorizontalPadding * (1 - 0.2f * progress)).toInt(),
            (listItemVerticalPadding * (1 - 0.2f * progress)).toInt(),
            (listItemHorizontalPadding * (1 - 0.2f * progress)).toInt(),
            (listItemVerticalPadding * (1 - 0.2f * progress)).toInt()
        )

        holder.itemView.list_item_fg.alpha = progress
    }

    private inline val LinearLayoutManager.visibleItemsRange: IntRange
        get() = findFirstVisibleItemPosition()..findLastVisibleItemPosition()

    fun getScaleDownAnimator(isScaledDown: Boolean): ValueAnimator {
        val lm = recyclerView.layoutManager as LinearLayoutManager

        val animator = getValueAnimator(
            isScaledDown,
            duration = 300L, interpolator = AccelerateDecelerateInterpolator()
        ) { progress ->
            for (i in lm.visibleItemsRange) {
                val holder = recyclerView.findViewHolderForLayoutPosition(i) as ItemViewHolder
                setScaleDownProgress(holder, i, progress)
            }
        }


        animator.doOnStart { this.isScaledDown = isScaledDown }

        animator.doOnEnd {
            repeat(lm.itemCount) { if (it !in lm.visibleItemsRange) notifyItemChanged(it) }
        }
        return animator
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


    }

    private fun scaleDownItem(holder: ItemViewHolder, position: Int, isScaleDown: Boolean) {
        setScaleDownProgress(holder, position, if (isScaleDown) 1f else 0f)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }


    override fun onViewAttachedToWindow(holder: ItemViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (expandedHeight < 0) {
            expandedHeight = 0

            holder.itemView.card_container.doOnLayout { view ->
                originalHeight = view.height

                holder.itemView.expand_view.isVisible = true
                view.doOnPreDraw {
                    expandedHeight = view.height
                    holder.itemView.expand_view.isVisible = false
                }
            }
        }
    }
}