package ite.smu.socketmanager_app;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

//    public static int PORT = 5555; // 서버포트번호
//    public String serverIP = ""; // 서버아이피주소

    Button logBtn, signBtn;
    EditText pwd, id, ip;
    // List<NameValuePair> nameValuePairs;
    TextView tv;
    private static final String TAG = "LoginActivity";
    private String hostv, idv, pwv;
    private int portv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        logBtn = (Button) findViewById(R.id.loginButton);
        signBtn = (Button) findViewById(R.id.signupButton);
        ip = (EditText) findViewById(R.id.ipInput);
        id = (EditText) findViewById(R.id.idInput);
        pwd = (EditText) findViewById(R.id.passwordInput);
        tv = (TextView) findViewById(R.id.textView2);

        logBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                hostv = ip.getText().toString().trim();
                //portv = 5555;
                idv = id.getText().toString().trim();
                pwv = pwd.getText().toString().trim();
                // msgSummit();
                //hostv = "192.168.0.52";
                // hostv = "192.168.0.13";
               // hostv = "192.168.43.246";

                //idv = "sng";
                //pwv = "sng";

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("host", hostv);
                bundle.putString("id", idv);
                bundle.putString("pw", pwv);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        });
    }
}