package up_ri.se.arbaroen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;


public class AutoStart extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Toast.makeText(context, "Boot finished -> launch buttons", Toast.LENGTH_LONG).show();

        //---start the service activity of the app---
        Intent i = new Intent(context, service.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startService(i);

    }

}