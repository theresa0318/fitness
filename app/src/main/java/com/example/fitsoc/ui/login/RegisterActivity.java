package  com.example.fitsoc.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitsoc.NavigationActivity;
import com.example.fitsoc.R;
import com.example.fitsoc.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity{
    private LoginViewModel loginViewModel;
    private ActivityRegisterBinding bindingR;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindingR = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(bindingR.getRoot());

        //Change button icon size
        Button button_register_email = (Button) findViewById(R.id.register_email);
        Drawable drawable_register_email = getResources().getDrawable(R.drawable.email_icon);
        drawable_register_email.setBounds(0, 0, 100, 100);
        button_register_email.setCompoundDrawables(drawable_register_email, null, null, null);

        Button button_register_wechat = (Button) findViewById(R.id.register_wechat);
        Drawable drawable_register_wechat = getResources().getDrawable(R.drawable.wechat_icon);
        drawable_register_wechat.setBounds(0, 0, 100, 100);
        button_register_wechat.setCompoundDrawables(drawable_register_wechat, null, null, null);

        //
        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameLoginEditText = bindingR.usernameRegister;
        final EditText passwordLoginEditText = bindingR.passwordRegister;
        final Button loginButton = bindingR.registerEmail;
        final ProgressBar loadingProgressBar = bindingR.loading;

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameLoginEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordLoginEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                }
                setResult(Activity.RESULT_OK);

                //Complete and destroy login activity once successful
                finish();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameLoginEditText.getText().toString(),
                        passwordLoginEditText.getText().toString());
            }
        };
        usernameLoginEditText.addTextChangedListener(afterTextChangedListener);
        passwordLoginEditText.addTextChangedListener(afterTextChangedListener);
        passwordLoginEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(usernameLoginEditText.getText().toString(),
                            passwordLoginEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel.login(usernameLoginEditText.getText().toString(),
                        passwordLoginEditText.getText().toString());
                //同login page, 也需要修改
                Intent intent = new Intent(RegisterActivity.this, NavigationActivity.class);
                startActivity(intent);
            }
        });
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) ;
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}
