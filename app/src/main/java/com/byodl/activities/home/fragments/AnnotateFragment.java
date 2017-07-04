package com.byodl.activities.home.fragments;

import java.io.File;

public class AnnotateFragment extends CameraFragment {
    private static final String TAG = AnnotateFragment.class.getSimpleName();

	@Override
	protected void updateLastImage(File f)
	{
		super.updateLastImage(null);
	}

	@Override
    protected void fileSaved(final File file) {
		AnnotateAskFragment f = AnnotateAskFragment.newInstance(file.getAbsolutePath());
		f.show(getChildFragmentManager(),"ANNOTATE");
    }

}
