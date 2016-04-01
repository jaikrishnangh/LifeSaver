package record.jaikrishnan.com.lifesaver;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Account extends AppCompatActivity {

    EditText num1,num2;
    Button add;
    String n1,n2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        num1 = (EditText) findViewById(R.id.et1);
        num2 = (EditText) findViewById(R.id.et2);
        add = (Button) findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                n1 = num1.getText().toString();
                n2 = num2.getText().toString();
                DB entry = new DB(Account.this);
                entry.open();
                entry.clean();
                entry.createEntry(n1);
                entry.createEntry(n2);
                String res = entry.getData();
                System.out.println(res);
                entry.close();
                Toast.makeText(Account.this,"Added Successfully",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Account.this,Shaker.class));
            }
        });
    }
        public String[] values(){
            DB entry = new DB(Account.this);
            entry.open();
            String res = entry.getData();
            String ans[] = res.split(" ");
            entry.close();
            return ans;
        }
}
