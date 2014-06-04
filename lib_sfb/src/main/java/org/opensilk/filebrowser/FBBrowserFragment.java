package org.opensilk.filebrowser;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

/**
 * Fragment container for the file lists. FileListFragment will use this fragments
 * ChildFragmentManager to manage list add/remove on tree traversal.
 *
 * Created by drew on 4/30/14.
 */
public class FBBrowserFragment extends Fragment implements FBBrowser, LoaderManager.LoaderCallbacks<List<FBItem>> {

    protected static final String SDCARD_ROOT;
    static {
        SDCARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    protected FBBrowserArgs mArgs;
    protected TextView mBreadcrumb;
    protected IFBSelectionListener mCallback;
    protected ActionMode mActionMode;

    @InjectView(android.R.id.list)
    protected CardListView mListView;
    @InjectView(android.R.id.empty)
    protected View mListEmptyView;

    protected CardArrayAdapter mAdapter;
    protected String mBreadCrumbPath;

    protected List<FBItem> mSelection = new ArrayList<>();
    protected Deque<Holder> mDirStack = new ArrayDeque<>();
    protected List<Card> mCurrentCards = new ArrayList<>();

    /**
     * Creates new instance
     * @param args
     * @return
     */
    public static FBBrowserFragment newInstance(FBBrowserArgs args) {
        FBBrowserFragment f = new FBBrowserFragment();
        Bundle b = new Bundle(1);
        b.putParcelable("fb__args", args);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mArgs = getArguments().getParcelable("fb__args");
        mAdapter = new CardArrayAdapter(getActivity(), new ArrayList<Card>());
        getLoaderManager().initLoader(0, getArguments(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fb__fragment_browser, container, false);
        ButterKnife.inject(this, v);
        // Android libraries dont have final id's??
        mBreadcrumb = ButterKnife.findById(v, R.id.fb__breadcrumb);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBreadCrumbPath = mArgs.getPath();
        setBreadCrumbText();

        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(mListEmptyView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    public void setSelectionListener(IFBSelectionListener l) {
        mCallback = l;
    }

    public void setBreadCrumbText() {
        if (!TextUtils.isEmpty(mBreadCrumbPath)) {
            if (mBreadCrumbPath.startsWith(SDCARD_ROOT)) {
                mBreadcrumb.setText(mBreadCrumbPath.replace(SDCARD_ROOT, "SDCARD"));
            } else {
                mBreadcrumb.setText(mBreadCrumbPath);
            }
        }
    }

    protected Card.OnCardClickListener getCardOnClickListener(int mediaType) {
        Card.OnCardClickListener l;
        switch (mediaType) {
            case FBItem.MediaType.UP_DIRECTORY:
                l = new Card.OnCardClickListener() {
                    @Override
                    public void onClick(Card card, View view) {
                        if (!popDirStack()) {
                            FBItem item = ((FBItemCard) card).getFile();
                            pushDirStack(item);
                        }
                    }
                };
                break;
            case FBItem.MediaType.DIRECTORY:
                l = new Card.OnCardClickListener() {
                    @Override
                    public void onClick(Card card, View view) {
                        FBItem item = ((FBItemCard) card).getFile();
                        pushDirStack(item);
                    }
                };
                break;
            default:
                l = new Card.OnCardClickListener() {
                    @Override
                    public void onClick(Card card, View view) {
                        FBItemCard c = (FBItemCard) card;
                        FBItem item = c.getFile();
                        if (!mSelection.contains(item)) {
                            mSelection.add(item);
                            c.setSelected(true);
                        } else {
                            mSelection.remove(item);
                            c.setSelected(false);
                        }
                        int num = mSelection.size();
                        if (num == 0) {
                            if (mActionMode != null) {
                                mActionMode.finish();
                            }
                            return;
                        }
                        if (mActionMode == null) {
                            mActionMode = getActivity().startActionMode(mActionModeCallback);
                        }
                        mActionMode.setTitle(getResources().getQuantityString(R.plurals.NitemsSelected, num, num));
                    }
                };
                break;
        }
        return l;
    }

    protected void pushDirStack(FBItem item) {
        Bundle b = new Bundle(1);
        b.putParcelable("fb__args", FBBrowserArgs.copy(mArgs).setPath(item.getPath()));
        mDirStack.push(new Holder(mBreadCrumbPath, mCurrentCards));
        mBreadCrumbPath = item.getPath();
        setBreadCrumbText();
        getLoaderManager().restartLoader(0, b, FBBrowserFragment.this);
    }

    /*
     * Implement FBBrowser
     */

    @Override
    public boolean popDirStack() {
        if (mDirStack.peek() != null) {
            Holder h = mDirStack.poll();
            mBreadCrumbPath = h.path;
            setBreadCrumbText();
            mCurrentCards.clear();
            mCurrentCards.addAll(h.list);
            mAdapter.clear();
            mAdapter.addAll(mCurrentCards);
            mAdapter.notifyDataSetChanged();
            return true;
        }
        return false;
    }


    /*
     * Loader callbacks
     */

    @Override
    public Loader<List<FBItem>> onCreateLoader(int id, Bundle args) {
        FBBrowserArgs _args = args.getParcelable("fb__args");
        return new FBItemArrayLoader(getActivity(), _args);
    }

    @Override
    public void onLoadFinished(Loader<List<FBItem>> loader, List<FBItem> data) {
        mCurrentCards.clear();
        if (data != null && data.size() > 0) {
            for (FBItem i : data) {
                FBItemCard c = new FBItemCard(getActivity(), i);
                c.setOnClickListener(getCardOnClickListener(c.getFile().getMediaType()));
                if (mSelection.contains(i)) {
                    c.setSelected(true);
                }
                mCurrentCards.add(c);
            }
        }
        mAdapter.clear();
        mAdapter.addAll(mCurrentCards);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<FBItem>> loader) {
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();
    }

    protected final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater mi = mode.getMenuInflater();
            mi.inflate(R.menu.fb__action_mode, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.fb__action_done) {
                mCallback.onFBItemsSelected(mSelection);
                mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mSelection.clear();
        }
    };

    private static final class Holder {
        final String path;
        final List<Card> list;
        Holder(String path, List<Card> list) {
            this.path = path;
            this.list = new ArrayList<>(list);
        }
    }

}
