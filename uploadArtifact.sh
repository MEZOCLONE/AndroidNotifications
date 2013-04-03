#!/bin/bash
cp /home/travis/build/mattem/AndroidNotifications/target/RemoteNotifier-1.apk /home/travis/build/mattem/AndroidNotifications/target/RemoteNotifier-${1}.apk
./dropbox_uploader.sh upload /home/travis/build/mattem/AndroidNotifications/target/RemoteNotifier-${1}.apk