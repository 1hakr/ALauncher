package dev.dworks.apps.alauncher.client;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.WindowManager.LayoutParams;

public final class WindowLayoutParams implements Parcelable {
   public LayoutParams layoutParams;
   public static final WindowLayoutParams.CREATOR CREATOR = new WindowLayoutParams.CREATOR();

   public final LayoutParams getLayoutParams() {
      return this.layoutParams;
   }

   public final void setLayoutParams(LayoutParams params) {
      this.layoutParams = params;
   }

   public final void readFromParcel(Parcel parcel) { ;
      Parcelable parcelable = parcel.readParcelable(LayoutParams.class.getClassLoader());
      this.layoutParams = (LayoutParams)parcelable;
   }

   public void writeToParcel(Parcel parcel, int flags) {
      LayoutParams params = this.layoutParams;

      parcel.writeParcelable((Parcelable)params, flags);
   }

   public int describeContents() {
      return 0;
   }

   public WindowLayoutParams(LayoutParams layoutParams) {
      this.layoutParams = layoutParams;
   }

   public WindowLayoutParams(Parcel parcel) {
      this.readFromParcel(parcel);
   }

   public static final class CREATOR implements Creator {
      public WindowLayoutParams createFromParcel(Parcel parcel) {
         return new WindowLayoutParams(parcel);
      }

      public WindowLayoutParams[] newArray(int size) {
         return new WindowLayoutParams[size];
      }

      private CREATOR() {
      }

   }
}
