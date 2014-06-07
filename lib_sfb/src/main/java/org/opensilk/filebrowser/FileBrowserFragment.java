package org.opensilk.filebrowser;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
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
public class FileBrowserFragment extends Fragment implements FileBrowser, LoaderManager.LoaderCallbacks<List<FileItem>> {

    protected static final String SDCARD_ROOT;
    static {
        SDCARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    protected FileBrowserArgs mArgs;
    protected TextView mBreadcrumb;
    protected FileSelectionListener mCallback;
    protected ActionMode mActionMode;

    @InjectView(android.R.id.list)
    protected CardListView mListView;
    @InjectView(android.R.id.empty)
    protected View mListEmptyView;

    protected FileCardAdapter mAdapter;
    protected String mBreadCrumbPath;

    protected List<FileItem> mSelection = new ArrayList<>();
    protected Deque<Holder> mDirStack = new ArrayDeque<>();

    /**
     * Creates new instance
     * @param args
     * @return
     */
    public static FileBrowserFragment newInstance(FileBrowserArgs args) {
        FileBrowserFragment f = new FileBrowserFragment();
        Bundle b = new Bundle(1);
        b.putParcelable("fb__args", args);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mArgs = getArguments().getParcelable("fb__args");
        mAdapter = new FileCardAdapter(getActivity(), new ArrayList<Card>());
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

    public void setSelectionListener(FileSelectionListener l) {
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
            case FileItem.MediaType.UP_DIRECTORY:
                l = new Card.OnCardClickListener() {
                    @Override
                    public void onClick(Card card, View view) {
                        if (!popDirStack()) {
                            FileItem item = ((FileItemCard) card).getFile();
                            pushDirStack(item);
                        }
                    }
                };
                break;
            case FileItem.MediaType.DIRECTORY:
                l = new Card.OnCardClickListener() {
                    @Override
                    public void onClick(Card card, View view) {
                        FileItem item = ((FileItemCard) card).getFile();
                        pushDirStack(item);
                    }
                };
                break;
            default:
                l = new Card.OnCardClickListener() {
                    @Override
                    public void onClick(Card card, View view) {
                        FileItemCard c = (FileItemCard) card;
                        FileItem item = c.getFile();
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

    protected void pushDirStack(FileItem item) {
        Bundle b = new Bundle(1);
        b.putParcelable("fb__args", FileBrowserArgs.copy(mArgs).setPath(item.getPath()));
        mDirStack.push(new Holder(mBreadCrumbPath, mAdapter.getCards()));
        mBreadCrumbPath = item.getPath();
        setBreadCrumbText();
        getLoaderManager().restartLoader(0, b, FileBrowserFragment.this);
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
            mAdapter.clear();
            mAdapter.addAll(h.list);
            return true;
        }
        return false;
    }


    /*
     * Loader callbacks
     */

    @Override
    public Loader<List<FileItem>> onCreateLoader(int id, Bundle args) {
        FileBrowserArgs _args = args.getParcelable("fb__args");
        return new FileItemArrayLoader(getActivity(), _args);
    }

    @Override
    public void onLoadFinished(Loader<List<FileItem>> loader, List<FileItem> data) {
        final List<Card> cards = new ArrayList<>();
        mAdapter.clear();
        if (data != null && data.size() > 0) {
            for (FileItem i : data) {
                FileItemCard c = new FileItemCard(getActivity(), i);
                c.setOnClickListener(getCardOnClickListener(c.getFile().getMediaType()));
                if (mSelection.contains(i)) {
                    c.setSelected(true);
                }
                cards.add(c);
            }
        }
        mAdapter.addAll(cards);
    }

    @Override
    public void onLoaderReset(Loader<List<FileItem>> loader) {
        mAdapter.clear();
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
                if (mCallback != null) {
                    mCallback.onFileItemsSelected(mSelection);
                }
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

    private static final class FileCardAdapter extends CardArrayAdapter {
        private final List<Card> cards;
        public FileCardAdapter(Context context, List<Card> cards) {
            super(context, cards);
            this.cards = cards;
        }
        public List<Card> getCards() { return cards; }
    }

}
