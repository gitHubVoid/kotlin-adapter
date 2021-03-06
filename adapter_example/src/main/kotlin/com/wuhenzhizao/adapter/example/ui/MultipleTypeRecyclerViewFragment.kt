package com.wuhenzhizao.adapter.example.ui

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.bigkoo.convenientbanner.ConvenientBanner
import com.bigkoo.convenientbanner.holder.Holder
import com.google.gson.Gson
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadmoreListener
import com.wuhenzhizao.adapter.*
import com.wuhenzhizao.adapter.example.R
import com.wuhenzhizao.adapter.example.bean.*
import com.wuhenzhizao.adapter.example.databinding.FragmentMultipleTypeRecyclerViewBinding
import com.wuhenzhizao.adapter.example.decoration.LinearOffsetsItemDecoration
import com.wuhenzhizao.adapter.example.image.DraweeImageView
import com.wuhenzhizao.adapter.example.image.GImageLoader
import com.wuhenzhizao.adapter.extension.addItems
import com.wuhenzhizao.adapter.extension.clear
import com.wuhenzhizao.adapter.extension.putItems
import com.wuhenzhizao.adapter.holder.RecyclerViewHolder
import com.wuhenzhizao.titlebar.utils.ScreenUtils

/**
 * Created by liufei on 2017/12/13.
 */
class MultipleTypeRecyclerViewFragment : BaseFragment<FragmentMultipleTypeRecyclerViewBinding>() {
    private lateinit var adapter: RecyclerViewAdapter<Any>
    private lateinit var productAdapter: RecyclerViewAdapter<Product>
    private lateinit var productList: List<Product>
    private lateinit var banners: BannerList
    private val PAGE_SIZE = 10
    private var currentPage = 1

    override fun getContentViewId(): Int = R.layout.fragment_multiple_type_recycler_view

    override fun initViews() {
        productList = Gson().fromJson<ProductList>(getString(R.string.products), ProductList::class.java).products
        banners = Gson().fromJson<BannerList>(getString(R.string.banners), BannerList::class.java)

        binding.rv.layoutManager = LinearLayoutManager(context)

        bindListener()
        bindAdapter()

        binding.refresh.autoRefresh(300)
    }

    private fun bindListener() {
        binding.refresh.setOnRefreshLoadmoreListener(object : OnRefreshLoadmoreListener {
            override fun onRefresh(refreshlayout: RefreshLayout) {
                simulateLoadData {
                    currentPage = 1
                    adapter.clear()
                    val list = arrayListOf<Any>()
                    list.add(banners)
                    list.add(Promotion())
                    list.add(Divider())
                    list.add(HeaderLine())
                    list.add(HeaderLineProductList(productList.subList(0, PAGE_SIZE)))
                    list.add(Divider())
                    list.add(Recommend())
                    (0 until PAGE_SIZE step 2)
                            .mapTo(list) { RecommendProducts(productList[it], productList[it + 1]) }
                    adapter.putItems(list)
                    refreshlayout.finishRefresh()
                    refreshlayout.isEnableLoadmore = true
                    refreshlayout.resetNoMoreData()
                }
            }

            override fun onLoadmore(refreshlayout: RefreshLayout) {
                simulateLoadData {
                    val list = mutableListOf<RecommendProducts>()
                    val pageStartIndex = currentPage * PAGE_SIZE
                    val pageEndIndex = Math.min((currentPage + 1) * PAGE_SIZE, productList.size)
                    (pageStartIndex until pageEndIndex step 2)
                            .mapTo(list) {
                                if (it + 1 < productList.size) {
                                    RecommendProducts(productList[it], productList[it + 1])

                                } else {
                                    RecommendProducts(productList[it], null)
                                }
                            }
                    adapter.addItems(list)
                    if (pageEndIndex < productList.size) {
                        refreshlayout.isEnableLoadmore = true
                        refreshlayout.finishLoadmore()
                    } else {
                        refreshlayout.isEnableLoadmore = false
                        refreshlayout.finishLoadmoreWithNoMoreData()
                    }
                    currentPage++
                }
            }
        })
    }

    private fun bindAdapter() {
//        adapter = RecyclerViewAdapter<Any>(context)
//                .match(BannerList::class, R.layout.item_multiple_type_recycler_view_banner)
//                .match(Promotion::class, R.layout.item_multiple_type_recycler_view_promotion)
//                .match(Divider::class, R.layout.item_multiple_type_recycler_view_divider)
//                .match(HeaderLine::class, R.layout.item_multiple_type_recycler_view_headine)
//                .match(HeaderLineProductList::class, R.layout.item_multiple_type_recycler_view_headine_product)
//                .match(Recommend::class, R.layout.item_multiple_type_recycler_view_recommend)
//                .match(RecommendProducts::class, R.layout.item_multiple_type_recycler_view_recommend_item)
//                .holderCreateInterceptor {
//                    onViewHolderCreate(it)
//                }
//                .holderBindInterceptor { position, viewHolder ->
//                    onViewHolderBind(position, viewHolder)
//                }
//                .clickInterceptor { position, holder ->
//
//                }
//                .attach(binding.rv)

        // replace multiple match() with layoutInterceptor
        adapter = RecyclerViewAdapter<Any>(context)
                .layoutInterceptor {
                    when (adapter.getItem(it)) {
                        is BannerList -> R.layout.item_multiple_type_recycler_view_banner
                        is Promotion -> R.layout.item_multiple_type_recycler_view_promotion
                        is Divider -> R.layout.item_multiple_type_recycler_view_divider
                        is HeaderLine -> R.layout.item_multiple_type_recycler_view_headine
                        is HeaderLineProductList -> R.layout.item_multiple_type_recycler_view_headine_product
                        is Recommend -> R.layout.item_multiple_type_recycler_view_recommend
                        is RecommendProducts -> R.layout.item_multiple_type_recycler_view_recommend_item
                        else -> {
                            0
                        }
                    }
                }
                .holderCreateInterceptor {
                    onViewHolderCreate(it)
                }
                .holderBindInterceptor { position, viewHolder ->
                    onViewHolderBind(position, viewHolder)
                }
                .clickInterceptor { position, holder ->

                }
                .attach(binding.rv)
    }

    private fun onViewHolderCreate(holder: RecyclerViewHolder) {
        when (holder.layoutId) {
            R.layout.item_multiple_type_recycler_view_banner -> {
                initBanner(holder)
            }
            R.layout.item_multiple_type_recycler_view_headine_product -> {
                initHeadLineProductList(holder)
            }
        }
    }

    private fun onViewHolderBind(position: Int, holder: RecyclerViewHolder) {
        val item = adapter.getItem(position)
        when (item) {
            is BannerList -> {
                bindBannerData(item, holder)
            }
            is Promotion -> {

            }
            is Divider -> {

            }
            is HeaderLine -> {

            }
            is HeaderLineProductList -> {
                bindHeadLineProductList(item, holder)
            }
            is Recommend -> {

            }
            is RecommendProducts -> {
                bindRecommendProducts(item, holder)
            }
        }
    }

    private fun initBanner(holder: RecyclerViewHolder) {
        holder.get<ConvenientBanner<Banner>>(R.id.banner, {
            setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.CENTER_HORIZONTAL)
            setPageIndicator(intArrayOf(R.drawable.banner_indicator_unselected, R.drawable.banner_indicator_selected))
            setOnItemClickListener {
                showToast("banner index $it clicked")
            }
        })
    }

    private fun bindBannerData(item: BannerList, holder: RecyclerViewHolder) {
        holder.get<ConvenientBanner<Banner>>(R.id.banner, {
            setPages({ BannerViewHolder() }, item.banners)
            startTurning(3000)
            setcurrentitem(it.currentItem)
        })
    }

    private class BannerViewHolder : Holder<Banner> {
        lateinit var iv: DraweeImageView

        override fun createView(context: Context): View {
            iv = LayoutInflater.from(context).inflate(R.layout.item_banner, null) as DraweeImageView
            return iv
        }

        override fun UpdateUI(context: Context, position: Int, data: Banner) {
            GImageLoader.displayUrl(context, iv, data.imageUrl)
        }

    }

    private fun initHeadLineProductList(holder: RecyclerViewHolder) {
        val recyclerView = holder.get<RecyclerView>(R.id.rv, {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            val decoration = LinearOffsetsItemDecoration(LinearOffsetsItemDecoration.LINEAR_OFFSETS_HORIZONTAL)
            decoration.setOffsetEdge(false)
            decoration.setOffsetLast(false)
            decoration.setItemOffsets(ScreenUtils.dp2PxInt(context, 12f))
            addItemDecoration(decoration)
        })

        productAdapter = RecyclerViewAdapter<Product>(context)
                .match(Product::class, R.layout.item_multiple_type_recycler_view_headine_item)
                .holderBindInterceptor { position, holder ->
                    val product = productAdapter.getItem(position)
                    holder.get<DraweeImageView>(R.id.iv, { GImageLoader.displayUrl(context, this, product.imageUrl) })
                    holder.get<TextView>(R.id.name, { text = product.name })
                    holder.get<TextView>(R.id.price, { text = "¥ ${product.price}" })
                }
                .attach(recyclerView)
    }

    private fun bindHeadLineProductList(item: HeaderLineProductList, holder: RecyclerViewHolder) {
        productAdapter.putItems(item.products)
    }

    private fun bindRecommendProducts(item: RecommendProducts, holder: RecyclerViewHolder) {
        holder.get<DraweeImageView>(R.id.left_iv, { GImageLoader.displayUrl(context, this, item.leftProduct.imageUrl) })
        holder.get<TextView>(R.id.left_name, { text = item.leftProduct.name })
        holder.get<TextView>(R.id.left_price, { text = "¥ ${item.leftProduct.price}" })
        holder.get<TextView>(R.id.left_reviews, { text = item.leftProduct.reviews })
        if (item.rightProduct != null) {
            holder.get<DraweeImageView>(R.id.right_iv, { GImageLoader.displayUrl(context, this, item.rightProduct.imageUrl) })
            holder.get<TextView>(R.id.right_name, { text = item.rightProduct.name })
            holder.get<TextView>(R.id.right_price, { text = "¥ ${item.rightProduct.price}" })
            holder.get<TextView>(R.id.right_reviews, { text = item.rightProduct.reviews })
            holder.get<RelativeLayout>(R.id.right_view, { visibility = View.VISIBLE })
        } else {
            holder.get<RelativeLayout>(R.id.right_view, { visibility = View.INVISIBLE })
        }
    }
}