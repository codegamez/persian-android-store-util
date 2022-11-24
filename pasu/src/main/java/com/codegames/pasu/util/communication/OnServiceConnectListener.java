package com.codegames.pasu.util.communication;

public interface OnServiceConnectListener {
	void connected();

	void couldNotConnect();
}