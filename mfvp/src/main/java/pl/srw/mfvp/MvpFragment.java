package pl.srw.mfvp;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import butterknife.ButterKnife;
import pl.srw.mfvp.view.delegate.LifeCycleListener;
import pl.srw.mfvp.view.delegate.LifeCycleNotifier;
import pl.srw.mfvp.view.delegate.presenter.PresenterOwner;
import pl.srw.mfvp.view.fragment.MvpActivityScopedFragment;
import pl.srw.mfvp.view.fragment.MvpFragmentScopedFragment;

/**
 * Parent class for fragments-view in MVP model.
 * Features:
 *  - dependency injection is done every time fragment is created
 *  - releasing dependencies depends on associated scope component
 *  - lifecycle events will be communicated to added listeners
 */
public abstract class MvpFragment extends Fragment {

    private LifeCycleNotifier notifier;

    public MvpFragment() {
        notifier = new LifeCycleNotifier();
    }

    @Override
    @CallSuper
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        injectDependencies();
        if (this instanceof PresenterOwner) {
            PresenterOwner presenterFragment = (PresenterOwner) this;
            addListener(presenterFragment.createPresenterDelegate());
        }
    }

    @Override
    @CallSuper
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @Override
    @CallSuper
    public void onStart() {
        super.onStart();
        notifier.notifyOnStart();
    }

    @Override
    @CallSuper
    public void onStop() {
        super.onStop();
        notifier.notifyOnStop();
    }

    @Override
    @CallSuper
    public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    /**
     * Callback when view if out of scope and all dependent objects need to be destroyed
     */
    @CallSuper
    void endOfScope() {
        notifier.notifyOnEnd();
        resetDependencies();
    }

    public MvpActivity getBaseActivity() {
        return (MvpActivity) super.getActivity();
    }

    /**
     * Add listener to this fragment lifecycle
     * @param listener    lifecycle listener
     */
    public final void addListener(LifeCycleListener listener) {
        notifier.register(listener);
    }

    private void injectDependencies() {
        if (this instanceof MvpFragmentScopedFragment) {
            final MvpFragmentScopedFragment fragment = (MvpFragmentScopedFragment) this;
            DependencyComponentManager.getInstance().getComponentFor(fragment).inject(fragment);
        } else if (this instanceof MvpActivityScopedFragment){
            final MvpActivityScopedFragment fragment = (MvpActivityScopedFragment) this;
            DependencyComponentManager.getInstance().getComponentFor(fragment).inject(fragment);
        } else {
            throw new ClassCastException("MvpFragment must implement " +
                    "one of interfaces: MvpFragmentScopedFragment or MvpActivityScopedFragment");
        }
    }

    private void resetDependencies() {
        if (this instanceof MvpFragmentScopedFragment) {
            final MvpFragmentScopedFragment fragment = (MvpFragmentScopedFragment) this;
            DependencyComponentManager.getInstance().releaseComponentFor(fragment);
        }
        // else dependencies will be reset by activity
    }
}