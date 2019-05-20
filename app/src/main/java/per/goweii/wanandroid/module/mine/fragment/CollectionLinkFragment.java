package per.goweii.wanandroid.module.mine.fragment;

import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import per.goweii.basic.core.base.BaseFragment;
import per.goweii.basic.core.utils.SmartRefreshUtils;
import per.goweii.basic.utils.ToastMaker;
import per.goweii.wanandroid.R;
import per.goweii.wanandroid.common.ScrollTop;
import per.goweii.wanandroid.event.CollectionEvent;
import per.goweii.wanandroid.event.SettingChangeEvent;
import per.goweii.wanandroid.module.main.activity.WebActivity;
import per.goweii.wanandroid.module.main.model.CollectionLinkBean;
import per.goweii.wanandroid.module.mine.adapter.CollectionLinkAdapter;
import per.goweii.wanandroid.module.mine.presenter.CollectionLinkPresenter;
import per.goweii.wanandroid.module.mine.view.CollectionLinkView;
import per.goweii.wanandroid.utils.RvAnimUtils;
import per.goweii.wanandroid.utils.SettingUtils;

/**
 * @author CuiZhen
 * @date 2019/5/17
 * QQ: 302833254
 * E-mail: goweii@163.com
 * GitHub: https://github.com/goweii
 */
public class CollectionLinkFragment extends BaseFragment<CollectionLinkPresenter> implements ScrollTop, CollectionLinkView {

    @BindView(R.id.srl)
    SmartRefreshLayout srl;
    @BindView(R.id.rv)
    RecyclerView rv;

    private SmartRefreshUtils mSmartRefreshUtils;
    private CollectionLinkAdapter mAdapter;

    public static CollectionLinkFragment create() {
        return new CollectionLinkFragment();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCollectionEvent(CollectionEvent event) {
        if (isDetached()) {
            return;
        }
        if (event.isCollect()) {
            presenter.getCollectLinkList();
        } else {
            if (event.getCollectId() != -1) {
                List<CollectionLinkBean> list = mAdapter.getData();
                for (int i = 0; i < list.size(); i++) {
                    CollectionLinkBean item = list.get(i);
                    if (item.getId() == event.getCollectId()) {
                        mAdapter.remove(i);
                        break;
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSettingChangeEvent(SettingChangeEvent event) {
        if (isDetached()) {
            return;
        }
        if (event.isRvAnimChanged()) {
            RvAnimUtils.setAnim(mAdapter, SettingUtils.getInstance().getRvAnim());
        }
    }

    @Override
    protected boolean isRegisterEventBus() {
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_collection_link;
    }

    @Nullable
    @Override
    protected CollectionLinkPresenter initPresenter() {
        return new CollectionLinkPresenter();
    }

    @Override
    protected void initView() {
        mSmartRefreshUtils = SmartRefreshUtils.with(srl);
        mSmartRefreshUtils.pureScrollMode();
        mSmartRefreshUtils.setRefreshListener(new SmartRefreshUtils.RefreshListener() {
            @Override
            public void onRefresh() {
                presenter.getCollectLinkList();
            }
        });
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new CollectionLinkAdapter();
        RvAnimUtils.setAnim(mAdapter, SettingUtils.getInstance().getRvAnim());
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                CollectionLinkBean item = mAdapter.getItem(position);
                if (item != null) {
                    WebActivity.start(getContext(), item.getName(), item.getLink());
                }
            }
        });
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                CollectionLinkBean item = mAdapter.getItem(position);
                if (item == null) {
                    return;
                }
                switch (view.getId()) {
                    default:
                        break;
                    case R.id.iv_remove:
                        presenter.uncollectLink(item);
                        break;
                }
            }
        });
        rv.setAdapter(mAdapter);
    }

    @Override
    protected void loadData() {
        presenter.getCollectLinkList();
    }

    @Override
    public void getCollectLinkListSuccess(int code, List<CollectionLinkBean> data) {
        mAdapter.setNewData(data);
        mSmartRefreshUtils.success();
    }

    @Override
    public void getCollectLinkListFailed(int code, String msg) {
        ToastMaker.showShort(msg);
        mSmartRefreshUtils.fail();
    }

    @Override
    public void scrollTop() {
        if (isAdded() && !isDetached()) {
            if (rv != null) {
                rv.smoothScrollToPosition(0);
            }
        }
    }
}
