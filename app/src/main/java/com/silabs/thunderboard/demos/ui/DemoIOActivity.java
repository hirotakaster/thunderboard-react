package com.silabs.thunderboard.demos.ui;

import static com.silabs.thunderboard.common.app.ThunderBoardConstants.POWER_SOURCE_TYPE_COIN_CELL;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.View;

import com.silabs.thunderboard.R;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;

import java.util.Random;

import javax.inject.Inject;

import butterknife.ButterKnife;

public class DemoIOActivity extends BaseDemoActivity implements DemoIOViewListener, LEDControl.OnCheckedChangeListener {

    public static boolean isDemoAllowed() {
        return true;
    }

    private static final int STATE_NORMAL = 0;
    private static final int STATE_PRESSED = 1;

    World b2World;

    private float timeStep = 1.0f/60.0f;
    private int iterations = 20;
    private float floorSpeed = 400.0f;
    private float ballMaxSpeed = 1000.0f;
    private int ballRadius = 20;
    private GameView myView;
    private final Object lock = new Object();
    private Body floorBody = null;
    private Random random = new Random();
    private Float nextFloat(Float min, Float max) {
        return min + random.nextFloat() * (max - min);
    }
    private Point displaySize;
    private int lastLedValue = 0;

    enum BodyType
    {
        Box,
        Floor,
        Wall,
        Ball
    }

    private int Button1PushStete = 0;

    @Inject
    DemoIOPresenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myView = new GameView(this);
        mainSection.addView(myView);
        ButterKnife.bind(this);
        component().inject(this);

        setButton0State(STATE_NORMAL);
        setButton1State(STATE_NORMAL);
        initControls();

        presenter.setViewListener(this, deviceAddress);
        displaySize = new Point(0, 0);
        Display display = this.getWindowManager().getDefaultDisplay();
        display.getSize(displaySize);

        // create gameworld and resize to diplay size
        b2World = new World(new Vec2(0.0f,9.81f));
        CreateBox(new Vec2(250.0f, displaySize.y - 350), new Vec2(80.0f,0.5f), BodyType.Floor);

        CreateBox(new Vec2(0.0f, 0.0f), new Vec2(displaySize.x,0.5f), BodyType.Wall);
        CreateBox(new Vec2(0.0f, 0.0f), new Vec2(0.5f,displaySize.y - 100), BodyType.Wall);
        CreateBox(new Vec2(displaySize.x, 0.0f), new Vec2(0.5f,displaySize.y - 100), BodyType.Wall);

        CreateBox(new Vec2(displaySize.x / 3, 250.0f), new Vec2(40.0f,40.0f), BodyType.Box);
        CreateBox(new Vec2(2 * (displaySize.x / 3 ), 350.0f), new Vec2(40.0f,40.0f), BodyType.Box);
        CreateBox(new Vec2( (displaySize.x / 4 ), 450.0f), new Vec2(40.0f,40.0f), BodyType.Box);

        CreateBall(new Vec2(nextFloat(10.0f, (float)displaySize.x - 10), nextFloat(10.0f, (float)200.0f)),
                   new Vec2(nextFloat(-1 * ballMaxSpeed, ballMaxSpeed), nextFloat(-1 * ballMaxSpeed, ballMaxSpeed)));
    }

    @Override
    protected BaseDemoPresenter getDemoPresenter() {
        return presenter;
    }

    @Override
    public int getToolbarColor() {
        return getResourceColor(R.color.sl_terbium_green);
    }

    @Override
    public String getToolbarString() {
        return getString(R.string.demo_io);
    }

    @Override
    public void setButton0State(int state) {
        synchronized(lock) {
            if (state == STATE_NORMAL) {
                if (floorBody != null) {
                    floorBody.setLinearVelocity(new Vec2(0.0f, 0.0f));
                }
                Button1PushStete = 0;
            } else if (state == STATE_PRESSED) {
                floorBody.setLinearVelocity(new Vec2(floorSpeed * -1, 0.0f));
                Button1PushStete = 1;
            }
        }
    }

    @Override
    public void setButton1State(int state) {
        synchronized(lock) {
            if (state == STATE_NORMAL) {
            } else if (state == STATE_PRESSED) {
                if (Button1PushStete == 1) {
                    CreateBall(new Vec2(nextFloat(10.0f, (float)displaySize.x - 10), nextFloat(10.0f, (float)200.0f)),
                               new Vec2(nextFloat(-1 * ballMaxSpeed, ballMaxSpeed), nextFloat(-1 * ballMaxSpeed, ballMaxSpeed)));
                    floorBody.setLinearVelocity(new Vec2(0.0f, 0.0f));
                } else {
                    floorBody.setLinearVelocity(new Vec2(floorSpeed, 0.0f));
                }

            }
        }
    }


    @Override
    public void onCheckedChanged(LEDControl ledControl, boolean isChecked) {
    }

    @Override
    public void setLed0State(int state) {
    }

    @Override
    public void setLed1State(int state) {
    }

    @Override
    public void setPowerSource(int powerSource) {
        switch (powerSource) {
            case POWER_SOURCE_TYPE_COIN_CELL:
                break;
            default:
                break;
        }
    }

    @Override
    public void initControls() {
    }

    private void CreateBall(Vec2 position, @Nullable Vec2 velocity) {
        Vec2 v = velocity == null ? new Vec2() : velocity;

        BodyDef bodyDef = new BodyDef();

        bodyDef.position = position;
        bodyDef.angle = 0.0f;
        bodyDef.linearVelocity = v;
        bodyDef.angularVelocity = 0.0f;
        bodyDef.fixedRotation = false;
        bodyDef.active = true;
        bodyDef.bullet = false;
        bodyDef.allowSleep = true;
        bodyDef.gravityScale = 1.0f;
        bodyDef.linearDamping = 0.0f;
        bodyDef.angularDamping = 0.0f;
        bodyDef.userData = (Object) BodyType.Ball;
        bodyDef.type = org.jbox2d.dynamics.BodyType.DYNAMIC;

        CircleShape shape = new CircleShape();
        shape.setRadius((float)ballRadius);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.userData = null;
        fixtureDef.friction = 0.00f;
        fixtureDef.restitution = 1.00f;
        fixtureDef.density = nextFloat(1.0f, 2.0f);
        fixtureDef.isSensor = false;
        fixtureDef.userData = (Object)BodyType.Ball;

        Body body = b2World.createBody(bodyDef);
        body.createFixture(fixtureDef);
    }

    private void CreateBox(Vec2 position1, Vec2 position2, BodyType floortype)  {
        BodyDef bodyDef = new BodyDef();

        bodyDef.position = position1;
        bodyDef.angle = 0.0f;
        bodyDef.linearVelocity = new Vec2(0.0f,0.0f);
        bodyDef.angularVelocity = 0.0f;
        bodyDef.fixedRotation = false;
        bodyDef.active = true;
        bodyDef.bullet = false;
        bodyDef.allowSleep = true;
        bodyDef.gravityScale = 0.75f;
        bodyDef.linearDamping = 0.0f;
        bodyDef.angularDamping = 0.0f;
        bodyDef.userData = (Object)floortype;
        bodyDef.type = org.jbox2d.dynamics.BodyType.KINEMATIC;

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(position2.x, position2.y);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.userData = null;
        fixtureDef.friction = 0.0f;
        fixtureDef.restitution = 1.00f;
        fixtureDef.density = 1.0f;
        fixtureDef.isSensor = false;
        fixtureDef.userData = (Object)floortype;

        Body body = b2World.createBody(bodyDef);
        if (floortype == BodyType.Floor)
            floorBody = body;
        body.createFixture(fixtureDef);
    }

    class GameView extends View{
        Canvas canvas;
        public GameView(Context context) {
            super(context);
        }
        public void drawBox(Body body) {
            Paint mPaint = new Paint();
            mPaint.setAntiAlias(true);
            if ((BodyType)body.getUserData() == BodyType.Floor) {
                mPaint.setColor(Color.RED);
                canvas.drawRect(body.getPosition().x - 80, body.getPosition().y, body.getPosition().x + 80, body.getPosition().y + 10, mPaint);
            }
            else if ((BodyType)body.getUserData() == BodyType.Box) {
                mPaint.setColor(Color.BLUE);
                canvas.drawRect(body.getPosition().x - 40, body.getPosition().y - 40
                        , body.getPosition().x + 40, body.getPosition().y + 40, mPaint);
            }
        }

        public void drawCircle(Body body) {
            Paint mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setColor(Color.GREEN);
            canvas.drawCircle(body.getPosition().x, body.getPosition().y, ballRadius, mPaint);
        }

        public void update(){
            synchronized(lock) {
                b2World.step(timeStep, iterations, iterations);
            }
            int action = 0;
            Resources res = getResources();
            for (Body body = b2World.getBodyList(); body != null; body = body.getNext()) {
                // lost ball
                if (body.getPosition().y >= floorBody.getPosition().y  + ballRadius) {
                    action |= res.getInteger(R.integer.led1_on);
                    b2World.destroyBody(body);
                }
            }

            if (b2World.getContactCount() > 0) {
                for (Contact contact = b2World.getContactList(); contact != null; contact = contact.getNext()) {
                    // hit ball and floor
                    if ((contact.getFixtureA().getUserData() == BodyType.Ball && contact.getFixtureB().getUserData() == BodyType.Floor) ||
                            (contact.getFixtureA().getUserData() == BodyType.Floor && contact.getFixtureB().getUserData() == BodyType.Ball))
                        action |= res.getInteger(R.integer.led0_on);
                }
            }
            if (lastLedValue != action) {
                presenter.ledAction(action);
                lastLedValue = action;
            }
            invalidate();
        }

        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            this.canvas = canvas;
            int num_objects = b2World.getBodyCount();
            if(num_objects >= 0) {
                for(Body body = b2World.getBodyList();body!=null;body=body.getNext())
                {
                    switch((BodyType)body.getUserData()) {
                        case Box:
                        case Floor:
                            drawBox(body);
                            break;
                        case Ball:
                            drawCircle(body);
                            break;
                    }
                }
            }
            update();
        }
    }
}
