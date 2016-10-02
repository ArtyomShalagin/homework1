package ru.ifmo.android_2016.calc;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Stack;
import java.util.StringTokenizer;

public final class CalculatorActivity extends Activity implements View.OnClickListener {

    final String ERR_MSG = "ERR";
    boolean dotUsed = false;
    Button[] numpad;
    Button addButton, subButton, divButton, mulButton, dotButton, eqButton, delButton;
    TextView textView;
    HashMap<Button, String> operations;

    private static final String KEY_SAVED_STRING = "saved_string";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        textView = (TextView) findViewById(R.id.text_view);
        textView.setVisibility(View.VISIBLE);
        numpad = new Button[10];
        for (int i = 0; i < 10; i++) {
            try {
                numpad[i] = (Button) findViewById(R.id.class.getField("d" + i).getInt(null));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        addButton = (Button) findViewById(R.id.add);
        subButton = (Button) findViewById(R.id.sub);
        mulButton = (Button) findViewById(R.id.mul);
        divButton = (Button) findViewById(R.id.div);
        dotButton = (Button) findViewById(R.id.dot);
        eqButton = (Button) findViewById(R.id.equals);
        delButton = (Button) findViewById(R.id.del);

        System.out.println(addButton);
        addButton.setOnClickListener(this);
        subButton.setOnClickListener(this);
        mulButton.setOnClickListener(this);
        divButton.setOnClickListener(this);
        dotButton.setOnClickListener(this);
        eqButton.setOnClickListener(this);
        delButton.setOnClickListener(this);

        for (Button b : numpad) {
            b.setOnClickListener(this);
        }
        textView.setMovementMethod(new ScrollingMovementMethod());

        operations = new HashMap<Button, String>() {{
            put(addButton, "+");
            put(subButton, "-");
            put(mulButton, "*");
            put(divButton, "/");
        }};

        if (savedInstanceState != null) {
            CharSequence savedText = savedInstanceState.getCharSequence(KEY_SAVED_STRING);
            if (savedText != null) {
                textView.setText(savedText);
            }
        }
    }

    private String rpn(String source) {
        StringTokenizer st = new StringTokenizer(source, "+-*/", true);
        Stack<BigDecimal> nums = new Stack<>();
        Stack<Character> ops = new Stack<>();
        String ans;
        try {
            if (st.countTokens() > 1) { //very bad, sorry :(
                String s = st.nextToken();
                if (isOperation(s.charAt(0))) {
                    switch (s) {
                        case "-":
                            nums.push(new BigDecimal(st.nextToken()).negate());
                            break;
                        case "+":
                            nums.add(new BigDecimal(st.nextToken()));
                            break;
                        default:
                            throw new IllegalArgumentException("wrong op at the beginning");
                    }
                } else {
                    nums.add(new BigDecimal(s));
                }
            }
            while (st.hasMoreTokens()) {
                String s = st.nextToken();
                if (isOperation(s.charAt(0))) {
                    char op = s.charAt(0);
                    while (!ops.empty() && priority(ops.peek()) >= priority(op)) {
                        processOperation(nums, ops.pop());
                    }
                    ops.add(op);
                } else {
                    BigDecimal val = new BigDecimal(s);
                    nums.add(val);
                }
            }
            while (!ops.empty()) {
                processOperation(nums, ops.pop());
            }
            ans = nums.peek().toPlainString();
            int ind = ans.length() - 1;
            if (ans.contains(".")) {
                while (ind > 1 && ans.charAt(ind) == '0')
                    ind--;
            }
            if (ans.charAt(ind) == '.')
                ind--;
            ans = ans.substring(0, ind + 1);
            dotUsed = ans.contains(".");
        } catch (Exception e) {
            e.printStackTrace();
            ans = ERR_MSG;
        }
        return ans;
    }

    private void processOperation(Stack<BigDecimal> stack, char op) {
        BigDecimal r = stack.pop(), l = stack.pop();
        switch (op) {
            case '+':
                stack.add(l.add(r));
                return;
            case '-':
                stack.add(l.subtract(r));
                return;
            case '*':
                stack.add(l.multiply(r));
                return;
            case '/':
                stack.add(l.divide(r, 10, BigDecimal.ROUND_HALF_EVEN));
                return;
            default:
                throw new IllegalArgumentException("Wrong operation");
        }
    }

    private boolean isOperation(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private int priority(char op) {
        if (op == '+' || op == '-')
            return 1;
        if (op == '*' || op == '/')
            return 2;
        return -1;
    }

    @Override
    public void onClick(View v) {
        CharSequence curr = textView.getText();
        if (ERR_MSG.equals(curr.toString())) {
            textView.setText("");
            curr = "";
        }
        for (int i = 0; i < 10; i++) {
            if (v.equals(numpad[i])) {
                textView.append("" + i);
                return;
            }
        }
        if (operations.containsKey(v)) {
            if (curr.length() != 0 && isOperation(curr.charAt(curr.length() - 1))) {
                curr = curr.subSequence(0, curr.length() - 1);
            }
            textView.setText(curr + operations.get(v));
            dotUsed = false;
        } else if (v.equals(dotButton)) {
            if (!dotUsed) {
                textView.append(".");
                dotUsed = true;
            }
        } else if (v.equals(delButton)) {
            if (curr.length() > 0) {
                if (curr.charAt(curr.length() - 1) == '.') {
                    dotUsed = false;
                }
                textView.setText(curr.subSequence(0, curr.length() - 1));
            }
        } else if (v.equals(eqButton)) {
            dotUsed = false;
            textView.setText(rpn(curr.toString()));
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(KEY_SAVED_STRING, textView.getText());
    }
}