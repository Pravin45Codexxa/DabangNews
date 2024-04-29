package com.app.dabangdeshnews.adapter;

import static com.app.dabangdeshnews.util.Constant.POST_PER_PAGE;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.solodroid.ads.sdk.util.Constant.ADMOB;
import static com.solodroid.ads.sdk.util.Constant.APPLOVIN;
import static com.solodroid.ads.sdk.util.Constant.APPLOVIN_DISCOVERY;
import static com.solodroid.ads.sdk.util.Constant.APPLOVIN_MAX;
import static com.solodroid.ads.sdk.util.Constant.FACEBOOK;
import static com.solodroid.ads.sdk.util.Constant.FAN;
import static com.solodroid.ads.sdk.util.Constant.GOOGLE_AD_MANAGER;
import static com.solodroid.ads.sdk.util.Constant.STARTAPP;
import static com.solodroid.ads.sdk.util.Constant.WORTISE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.dabangdeshnews.Config;
import com.app.dabangdeshnews.R;
import com.app.dabangdeshnews.activity.ActivityCategoryDetail;
import com.app.dabangdeshnews.database.prefs.AdsPref;
import com.app.dabangdeshnews.database.prefs.SharedPref;
import com.app.dabangdeshnews.model.Post;
import com.app.dabangdeshnews.util.AdsManager;
import com.app.dabangdeshnews.util.Constant;
import com.app.dabangdeshnews.util.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.solodroid.ads.sdk.format.NativeAdViewHolder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.List;

public class AdapterPost extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_PROG = 0;
    private final int VIEW_ITEM = 1;
    private final int VIEW_HEAD = 2;
    private final int VIEW_AD = 3;
    List<Post> posts;
    Context context;
    private OnItemClickListener mOnItemClickListener;
    private OnItemOverflowClickListener mOnItemOverflowClickListener;
    AdapterCategoryList adapter;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    boolean scrolling = false;
    SharedPref sharedPref;
    AdsPref adsPref;
    AdsManager adsManager;
    boolean isHeader;

    public interface OnItemClickListener {
        void onItemClick(View view, Post obj, int position);
    }

    public interface OnItemOverflowClickListener {
        void onItemOverflowClick(View view, Post obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public void setOnItemOverflowClickListener(final OnItemOverflowClickListener mItemOverflowClickListener) {
        this.mOnItemOverflowClickListener = mItemOverflowClickListener;
    }

    public AdapterPost(Context context, RecyclerView view, List<Post> posts, boolean isHeader) {
        this.posts = posts;
        this.context = context;
        this.sharedPref = new SharedPref(context);
        this.adsPref = new AdsPref(context);
        this.adsManager = new AdsManager((Activity) context);
        this.isHeader = isHeader;
        lastItemViewDetector(view);
        view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    scrolling = true;
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    scrolling = false;
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public TextView txtTitle;
        public TextView txtDescription;
        public TextView txtDate;
        public ImageView icDate;
        public View lytUncategorized;
        public LinearLayout lytLabel;
        public TextView txtAlphabet;
        public ImageView thumbnailImage;
        public ImageView imgOverflow;
        public RecyclerView recyclerView;
        public LinearLayout lytDate;
        public LinearLayout lytParent;

        public OriginalViewHolder(View v) {
            super(v);
            txtTitle = v.findViewById(R.id.txt_title);
            txtDescription = v.findViewById(R.id.txt_description);
            txtDate = v.findViewById(R.id.txt_date);
            icDate = v.findViewById(R.id.ic_date);
            lytUncategorized = v.findViewById(R.id.txt_label_uncategorized);
            lytLabel = v.findViewById(R.id.lyt_label);
            txtAlphabet = v.findViewById(R.id.txt_alphabet);
            thumbnailImage = v.findViewById(R.id.thumbnail_image);
            imgOverflow = v.findViewById(R.id.img_overflow);
            recyclerView = v.findViewById(R.id.recycler_view);
            lytDate = v.findViewById(R.id.lyt_date);
            lytParent = v.findViewById(R.id.lyt_parent);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_HEAD) {
            View v;
            if (isHeader) {
                if (Config.ENABLE_NEW_APP_DESIGN) {
                    v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_post_heading, parent, false);
                } else {
                    v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_heading, parent, false);
                }
            } else {
                if (Config.ENABLE_NEW_APP_DESIGN) {
                    v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_post, parent, false);
                } else {
                    v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
                }
            }
            vh = new OriginalViewHolder(v);
        } else if (viewType == VIEW_ITEM) {
            View v;
            if (sharedPref.getIsShowPostListInLargeStyle()) {
                if (Config.ENABLE_NEW_APP_DESIGN) {
                    v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_post_heading, parent, false);
                } else {
                    v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_heading, parent, false);
                }
            } else {
                if (Config.ENABLE_NEW_APP_DESIGN) {
                    v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_post, parent, false);
                } else {
                    v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
                }
            }
            vh = new OriginalViewHolder(v);
        } else if (viewType == VIEW_AD) {
            View v;
            if (adsPref.getNativeAdStylePostList().equals("small")) {
                v = LayoutInflater.from(parent.getContext()).inflate(com.solodroid.ads.sdk.R.layout.view_native_ad_radio, parent, false);
            } else if (adsPref.getNativeAdStylePostList().equals("medium")) {
                v = LayoutInflater.from(parent.getContext()).inflate(com.solodroid.ads.sdk.R.layout.view_native_ad_news, parent, false);
            } else {
                v = LayoutInflater.from(parent.getContext()).inflate(com.solodroid.ads.sdk.R.layout.view_native_ad_medium, parent, false);
            }
            vh = new NativeAdViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_load_more, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final Post p = posts.get(position);
            final OriginalViewHolder vItem = (OriginalViewHolder) holder;

            Document htmlData = Jsoup.parse(p.content);

            vItem.txtTitle.setText(p.title);

            if (sharedPref.showShortDescription()) {
                vItem.txtTitle.setMaxLines(2);
                vItem.txtDescription.setText(Tools.parseHtml(htmlData.text()));
            } else {
                vItem.txtTitle.setMaxLines(3);
                vItem.txtDescription.setVisibility(View.GONE);
            }

            if (sharedPref.showPostDate()) {
                vItem.txtDate.setText(Tools.convertDateTime(p.published, null));
            } else {
                vItem.txtDate.setVisibility(View.GONE);
                vItem.lytDate.setVisibility(View.GONE);
            }

            vItem.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            adapter = new AdapterCategoryList(context, p.labels);
            vItem.recyclerView.setAdapter(adapter);
            adapter.setOnItemClickListener((view, items, pos) -> {
                Intent intent = new Intent(context, ActivityCategoryDetail.class);
                intent.putExtra(Constant.EXTRA_OBJC, items.get(pos));
                context.startActivity(intent);
            });

            if (p.labels.size() >= 1) {
                vItem.lytUncategorized.setVisibility(View.GONE);
            } else {
                vItem.lytUncategorized.setVisibility(View.VISIBLE);
                vItem.lytUncategorized.setOnClickListener(view -> {
                });
            }

            if (sharedPref.getIsDarkTheme()) {
                vItem.lytUncategorized.setBackgroundResource(R.drawable.bg_chips_dark);
                vItem.imgOverflow.setColorFilter(ContextCompat.getColor(context, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
                vItem.icDate.setColorFilter(ContextCompat.getColor(context, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
            } else {
                vItem.lytUncategorized.setBackgroundResource(R.drawable.bg_chips_default);
                vItem.imgOverflow.setColorFilter(ContextCompat.getColor(context, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
                vItem.icDate.setColorFilter(ContextCompat.getColor(context, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
            }

            RequestOptions requestOptions;
            if (sharedPref.getIsShowPostListInLargeStyle()) {
                requestOptions = new RequestOptions().override(Constant.HEADER_WIDTH, Constant.HEADER_HEIGHT);
            } else {
                if (sharedPref.showPostHeader() && position == 0) {
                    requestOptions = new RequestOptions().override(Constant.HEADER_WIDTH, Constant.HEADER_HEIGHT);
                } else {
                    requestOptions = new RequestOptions().override(Constant.THUMBNAIL_WIDTH, Constant.THUMBNAIL_HEIGHT);
                }
            }

            Elements elements = htmlData.select("img");
            if (elements.hasAttr("src")) {
                Glide.with(context)
                        .load(elements.get(0).attr("src").replace(" ", "%20"))
                        .apply(requestOptions)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.bg_button_transparent)
                        .centerCrop()
                        .into(vItem.thumbnailImage);
                vItem.txtAlphabet.setVisibility(View.GONE);
            } else {
                vItem.thumbnailImage.setImageResource(R.drawable.bg_button_transparent);
                vItem.txtAlphabet.setVisibility(View.VISIBLE);
                vItem.txtAlphabet.setText(p.title.substring(0, 1));
            }


            vItem.lytParent.setOnClickListener(view -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, p, position);
                }
            });

            vItem.imgOverflow.setOnClickListener(view -> {
                if (mOnItemOverflowClickListener != null) {
                    mOnItemOverflowClickListener.onItemOverflowClick(view, p, position);
                }
            });

        } else if (holder instanceof NativeAdViewHolder) {

            final NativeAdViewHolder vItem = (NativeAdViewHolder) holder;
            final SharedPref sharedPref = new SharedPref(context);

            if (adsPref.getAdStatus()) {
                vItem.loadNativeAd(context,
                        "1",
                        1,
                        adsPref.getMainAds(),
                        adsPref.getBackupAds(),
                        adsPref.getAdMobNativeId(),
                        adsPref.getAdManagerNativeId(),
                        adsPref.getFanNativeId(),
                        adsPref.getAppLovinNativeAdManualUnitId(),
                        adsPref.getAppLovinBannerMrecZoneId(),
                        adsPref.getWortiseNativeAdUnitId(),
                        sharedPref.getIsDarkTheme(),
                        false,
                        adsPref.getNativeAdStylePostList(),
                        R.color.color_light_native_ad_background,
                        R.color.color_dark_native_ad_background
                );
                vItem.setNativeAdMargin(
                        context.getResources().getDimensionPixelOffset(R.dimen.gnt_no_margin),
                        context.getResources().getDimensionPixelOffset(R.dimen.item_post_padding_small),
                        context.getResources().getDimensionPixelOffset(R.dimen.gnt_no_margin),
                        context.getResources().getDimensionPixelOffset(R.dimen.item_post_padding_small)
                );
            }

        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }

        if (getItemViewType(position) == VIEW_PROG) {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setFullSpan(true);
        } else if (getItemViewType(position) == VIEW_HEAD) {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setFullSpan(true);
        } else if (getItemViewType(position) == VIEW_AD) {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setFullSpan(true);
        } else {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setFullSpan(false);
        }

    }

    public void insertData(List<Post> items) {
        setLoaded();
        int positionStart = getItemCount();
        int itemCount = items.size();
        this.posts.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    public void insertDataWithNativeAd(List<Post> posts) {
        setLoaded();
        int positionStart = getItemCount();
        if (adsPref.getAdStatus() && adsPref.getIsNativePostList()) {
            if (posts.size() >= adsPref.getNativeAdIndex())
                posts.add(adsPref.getNativeAdIndex(), new Post());
        }
        int itemCount = posts.size();
        this.posts.addAll(posts);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    @SuppressWarnings("SuspiciousListRemoveInLoop")
    public void setLoaded() {
        loading = false;
        for (int i = 0; i < getItemCount(); i++) {
            if (posts.get(i) == null) {
                posts.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public void setLoading() {
        if (getItemCount() != 0) {
            this.posts.add(null);
            notifyItemInserted(getItemCount() - 1);
            loading = true;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void resetListData() {
        this.posts.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    @Override
    public int getItemViewType(int position) {
        Post post = posts.get(position);
        if (post != null) {
            if (post.title == null || post.title.equals("")) {
                return VIEW_AD;
            }
            if (position == 0) {
                if (sharedPref.showPostHeader()) {
                    return VIEW_HEAD;
                } else {
                    return VIEW_ITEM;
                }
            } else {
                return VIEW_ITEM;
            }
        } else {
            return VIEW_PROG;
        }
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void lastItemViewDetector(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            final StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int lastPos = getLastVisibleItem(layoutManager.findLastVisibleItemPositions(null));
                    if (!loading && lastPos == getItemCount() - 1 && onLoadMoreListener != null) {
                        if (adsPref.getAdStatus() && adsPref.getIsNativePostList()) {
                            switch (adsPref.getMainAds()) {
                                case ADMOB:
                                case GOOGLE_AD_MANAGER:
                                case FAN:
                                case FACEBOOK:
                                case STARTAPP:
                                case APPLOVIN:
                                case APPLOVIN_MAX:
                                case APPLOVIN_DISCOVERY:
                                case WORTISE: {
                                    //posts per page plus 1 Ad
                                    int current_page = getItemCount() / (POST_PER_PAGE + 1);
                                    onLoadMoreListener.onLoadMore(current_page);
                                    break;
                                }
                                default: {
                                    int current_page = getItemCount() / POST_PER_PAGE;
                                    onLoadMoreListener.onLoadMore(current_page);
                                    break;
                                }
                            }
                        } else {
                            int current_page = getItemCount() / POST_PER_PAGE;
                            onLoadMoreListener.onLoadMore(current_page);
                        }
                        loading = true;
                    }
                }
            });
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

    private int getLastVisibleItem(int[] into) {
        int lastIdx = into[0];
        for (int i : into) {
            if (lastIdx < i) lastIdx = i;
        }
        return lastIdx;
    }

}