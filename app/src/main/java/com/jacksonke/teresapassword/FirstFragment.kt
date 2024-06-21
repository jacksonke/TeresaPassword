package com.jacksonke.teresapassword


import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.ListFragment
import com.jacksonke.teresapassword.databinding.FragmentFirstBinding
import java.lang.ref.WeakReference


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : ListFragment(), MyArrayAdapter.OnItemSubViewClickListener
    , SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    companion object {
        const val TAG = "FirstFragment"
    }
    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    var initialQuery : CharSequence? = null

    private val STATE_QUERY = "q"
    private val STATE_MODEL = "m"

    internal class MyHandler(fragment: FirstFragment?) : Handler(){
        private var fragmentRef: WeakReference<FirstFragment?> = WeakReference(null)


        init {
            fragmentRef = WeakReference<FirstFragment?>(fragment)
        }

        override fun handleMessage(msg: Message) {
            val fragment: FirstFragment = fragmentRef.get() ?: return
            val adapter : MyArrayAdapter<*>? = fragment.listAdapter as? MyArrayAdapter<*>
            if (msg.what == Constants.MSG_ITEM_ADDED) {
                Log.d(TAG, "handleMessage: new item added")

                val siteName = msg.obj as String
                val entity: SiteEntity? = DBHelper.instance.queryBySite(siteName)
                Log.d(TAG, "handleMessage: MSG_ITEM_ADDED: entity=$entity")

                adapter?.add(entity)
                adapter?.notifyDataSetChanged()
            } else if (msg.what == Constants.MSG_ITEM_UPDATED) {
                Log.d(TAG, "handleMessage: item updated")
                adapter?.notifyDataSetChanged()
            }
        }
    }

    var mHandler: Handler = MyHandler(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // The usage of an interface lets you inject your own implementation
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.actions, menu)
                configureSearchView(menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_id_add -> addEntity()
//                    R.id.menu_id_search -> true
                    else -> true
                }
            }
            // Switch FirstFragment to FragmentSettings and then back to FirstFragment,
            // new FirstFragment will be created. So it is necessary to delete menuprovider
            // when old FirstFragment is destroyed.
        }, viewLifecycleOwner)

        if (savedInstanceState == null){
            initAdapter()
        } else {
            initAdapter()
            initialQuery=savedInstanceState.getCharSequence(STATE_QUERY);
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        val adapter : MyArrayAdapter<*>? = listAdapter as? MyArrayAdapter<*>
        val pass = Generator.instance()
            .generate(adapter!!.getItem(position) as SiteEntity)
        Toast.makeText(
            activity, pass,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val menuHost: MenuHost = requireActivity()
        _binding = null
    }

    private fun configureSearchView(menu: Menu) {
        val menuItemSearch = menu.findItem(R.id.menu_id_search);
        val searchView = menuItemSearch.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        searchView.setOnCloseListener(this)
        searchView.isSubmitButtonEnabled = false
        searchView.setIconifiedByDefault(true)
        if (initialQuery != null) {
            searchView.isIconified = false
            menuItemSearch.expandActionView()
            searchView.setQuery(initialQuery, true)
        }
    }


    private fun initAdapter() {
        val myArrayAdapter: MyArrayAdapter<SiteEntity> = MyArrayAdapter<SiteEntity>(
            requireActivity(),
            R.layout.list_item_view,
            R.id.text1,
            DBHelper.instance.queryAll()
        )
        myArrayAdapter.setOnItemSubViewClickListener(this)
        listAdapter = myArrayAdapter
    }

    override fun onItemSubViewDelClicked(position: Int, view: View) {

        if (view.id == R.id.btn_del) {
            // kotlin safe cast
            val adapter : MyArrayAdapter<*>? = listAdapter as? MyArrayAdapter<*>
            val siteEntity = adapter?.getItem(position)
            if (siteEntity is SiteEntity){
                // kotlin smart cast
                DBHelper.instance.rmSiteEntity(siteEntity.name)
                adapter.remove(siteEntity)
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onItemSubViewEditClicked(position: Int, view: View) {
        if (view.id == R.id.btn_edit) {
            val ft = requireActivity().supportFragmentManager.beginTransaction()
            val prev = requireActivity().supportFragmentManager.findFragmentByTag("dialog")
            if (prev != null) {
                ft.remove(prev)
            }
            ft.addToBackStack(null)

            // kotlin safe cast
            val adapter : MyArrayAdapter<*>? = listAdapter as? MyArrayAdapter<*>
            val siteEntity = adapter?.getItem(position)

            // Create and show the dialog.
            val newFragment = EditItemFragment()
            if (siteEntity is SiteEntity){
                newFragment.setTarget(siteEntity)
            }

            newFragment.setHandler(mHandler)
            newFragment.show(ft, "dialog")
        }
    }

    fun addEntity():Boolean {

        val ft: FragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()

        val prev: Fragment? = requireActivity().supportFragmentManager.findFragmentByTag("dialog")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)

        // Create and show the dialog.
        val newFragment = AddEntityFragment()
        newFragment.setHandler(mHandler)
        newFragment.show(ft, "dialog")
        return true
    }

    fun editEntity():Boolean{
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        val adapter : MyArrayAdapter<*>? = listAdapter as? MyArrayAdapter<*>
        if (newText.isNullOrEmpty()){
            adapter?.filter?.filter("")
        } else {
            adapter?.filter?.filter(newText)
        }
        return true
    }

    override fun onClose(): Boolean {
        val adapter : MyArrayAdapter<*>? = listAdapter as? MyArrayAdapter<*>
        adapter!!.filter.filter("")
        return true
    }

}