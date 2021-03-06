package com.polestar.clone.client.hook.proxies.job;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobWorkItem;
import android.content.Context;
import android.os.Build;

import com.polestar.clone.client.hook.base.MethodProxy;
import com.polestar.clone.client.hook.base.BinderInvocationProxy;
import com.polestar.clone.client.ipc.VJobScheduler;

import java.lang.reflect.Method;

import mirror.android.app.job.IJobScheduler;

/**
 * @author Lody
 *
 * @see android.app.job.JobScheduler
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobServiceStub extends BinderInvocationProxy {

	public JobServiceStub() {
		super(IJobScheduler.Stub.asInterface, Context.JOB_SCHEDULER_SERVICE);
	}

	@Override
	protected void onBindMethods() {
		super.onBindMethods();
		addMethodProxy(new schedule());
		addMethodProxy(new getAllPendingJobs());
		addMethodProxy(new cancelAll());
		addMethodProxy(new cancel());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			addMethodProxy(new enqueue());
		}
	}


	private class schedule extends MethodProxy {

		@Override
		public String getMethodName() {
			return "schedule";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			JobInfo jobInfo = (JobInfo) args[0];
			return VJobScheduler.get().schedule(jobInfo);
		}
	}

	private class getAllPendingJobs extends MethodProxy {

		@Override
		public String getMethodName() {
			return "getAllPendingJobs";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			return VJobScheduler.get().getAllPendingJobs();
		}
	}

	private class cancelAll extends MethodProxy {

		@Override
		public String getMethodName() {
			return "cancelAll";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			VJobScheduler.get().cancelAll();
			return 0;
		}
	}

	private class cancel extends MethodProxy {

		@Override
		public String getMethodName() {
			return "cancel";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			int jobId = (int) args[0];
			VJobScheduler.get().cancel(jobId);
			return 0;
		}
	}

	// int enqueue(JobInfo job, JobWorkItem work);
	private class enqueue extends MethodProxy {

		@Override
		public String getMethodName() {
			return "enqueue";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			JobInfo job = (JobInfo) args[0];
			JobWorkItem work = (JobWorkItem) args[1];
			VJobScheduler.get().enqueue(job, work);
			return 0;
		}
	}
}
