package com.jacksonke.teresapassword

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

class ItemListAdapter<T> : ArrayAdapter<Any?> {
    private var itemSubViewClickListener: OnItemSubViewClickListener? = null

    constructor(context: Context, resource: Int) : super(context, resource)
    constructor(context: Context?, resource: Int, textViewResourceId: Int) : super(
        context!!, resource, textViewResourceId
    )

    constructor(context: Context, resource: Int, objects: Array<Any?>) : super(
        context, resource, objects)

    constructor(
        context: Context,
        resource: Int,
        textViewResourceId: Int,
        objects: Array<Any?>
    ) : super(
        context, resource, textViewResourceId, objects
    )

    constructor(context: Context, resource: Int, objects: List<*>) : super(
        context, resource, objects)

    constructor(
        context: Context,
        resource: Int,
        textViewResourceId: Int,
        objects: List<*>
    ) : super(
        context, resource, textViewResourceId, objects
    )

    fun setOnItemSubViewClickListener(onItemSubViewClickListener: OnItemSubViewClickListener?) {
        itemSubViewClickListener = onItemSubViewClickListener
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val view = super.getView(position, convertView, parent)
        view.findViewById<View>(R.id.btn_del).setOnClickListener { v ->
            if (itemSubViewClickListener != null) {
                itemSubViewClickListener!!.onItemSubViewDelClicked(position, v)
            }
        }
        view.findViewById<View>(R.id.btn_edit).setOnClickListener { v ->
            if (itemSubViewClickListener != null) {
                itemSubViewClickListener!!.onItemSubViewEditClicked(position, v)
            }
        }
        return view
    }

    interface OnItemSubViewClickListener {
        fun onItemSubViewDelClicked(position: Int, view: View)
        fun onItemSubViewEditClicked(position: Int, view: View)
    }

    companion object {
        private const val TAG = "ItemListAdapter"
    }
}