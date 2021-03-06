/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.stunner.client.widgets.canvas.view;

import org.junit.Before;
import org.junit.Test;
import org.kie.workbench.common.stunner.core.client.canvas.event.mouse.CanvasMouseDownEvent;
import org.kie.workbench.common.stunner.core.client.canvas.event.mouse.CanvasMouseUpEvent;
import org.kie.workbench.common.stunner.core.client.event.keyboard.KeyDownEvent;
import org.kie.workbench.common.stunner.core.client.event.keyboard.KeyPressEvent;
import org.kie.workbench.common.stunner.core.client.event.keyboard.KeyUpEvent;
import org.kie.workbench.common.stunner.core.client.event.keyboard.KeyboardEvent;
import org.mockito.Mock;
import org.uberfire.mocks.EventSourceMock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class LienzoPanelTest {

    @Mock
    private EventSourceMock<KeyPressEvent> keyPressEvent;

    @Mock
    private EventSourceMock<KeyDownEvent> keyDownEvent;

    @Mock
    private EventSourceMock<KeyUpEvent> keyUpEvent;

    @Mock
    private EventSourceMock<CanvasMouseDownEvent> mouseDownEvent;

    @Mock
    private EventSourceMock<CanvasMouseUpEvent> mouseUpEvent;

    private LienzoPanel lienzoPanel;

    @Before
    public void setup() {
        initMocks(this);
        lienzoPanel = new LienzoPanel(keyPressEvent,
                                      keyDownEvent,
                                      keyUpEvent,
                                      mouseDownEvent,
                                      mouseUpEvent);
    }

    @Test
    public void testOnFocus() {
        lienzoPanel.onFocus();
        assertEquals(lienzoPanel.isFocused(), true);
    }

    @Test
    public void testOnBlur() {
        lienzoPanel.onBlur();
        assertEquals(lienzoPanel.isFocused(), false);
    }

    @Test
    public void testOnMouseOver() {
        lienzoPanel.onMouseOver();
        assertEquals(lienzoPanel.isListening(), true);
    }

    @Test
    public void testOnMouseOut() {
        lienzoPanel.onMouseOut();
        assertEquals(lienzoPanel.isListening(), false);
    }

    @Test
    public void testOnMouseDown() {
        lienzoPanel.onMouseOut();
        lienzoPanel.onMouseDown();
        verify(mouseDownEvent, never()).fire(any(CanvasMouseDownEvent.class));

        lienzoPanel.onMouseOver();
        lienzoPanel.onMouseDown();
        verify(mouseDownEvent, times(1)).fire(any(CanvasMouseDownEvent.class));
    }

    @Test
    public void testOnKeyPress() {
        lienzoPanel.onMouseOut();
        lienzoPanel.onBlur();
        lienzoPanel.onKeyPress(KeyboardEvent.Key.DELETE.getUnicharCode());
        verify(keyPressEvent, never()).fire(any(KeyPressEvent.class));

        lienzoPanel.onMouseOut();
        lienzoPanel.onFocus();
        lienzoPanel.onKeyPress(KeyboardEvent.Key.DELETE.getUnicharCode());
        verify(keyPressEvent, never()).fire(any(KeyPressEvent.class));

        lienzoPanel.onMouseOver();
        lienzoPanel.onBlur();
        lienzoPanel.onKeyPress(KeyboardEvent.Key.DELETE.getUnicharCode());
        verify(keyPressEvent, never()).fire(any(KeyPressEvent.class));

        lienzoPanel.onMouseOver();
        lienzoPanel.onFocus();
        lienzoPanel.onKeyPress(KeyboardEvent.Key.DELETE.getUnicharCode());
        verify(keyPressEvent, times(1)).fire(any(KeyPressEvent.class));
    }

    @Test
    public void testOnKeyDown() {
        lienzoPanel.onMouseOut();
        lienzoPanel.onBlur();
        lienzoPanel.onKeyDown(KeyboardEvent.Key.DELETE.getUnicharCode());
        verify(keyDownEvent, never()).fire(any(KeyDownEvent.class));

        lienzoPanel.onMouseOut();
        lienzoPanel.onFocus();
        lienzoPanel.onKeyDown(KeyboardEvent.Key.DELETE.getUnicharCode());
        verify(keyDownEvent, never()).fire(any(KeyDownEvent.class));

        lienzoPanel.onMouseOver();
        lienzoPanel.onBlur();
        lienzoPanel.onKeyDown(KeyboardEvent.Key.DELETE.getUnicharCode());
        verify(keyDownEvent, never()).fire(any(KeyDownEvent.class));

        lienzoPanel.onMouseOver();
        lienzoPanel.onFocus();
        lienzoPanel.onKeyDown(KeyboardEvent.Key.DELETE.getUnicharCode());
        verify(keyDownEvent, times(1)).fire(any(KeyDownEvent.class));
    }

    @Test
    public void testOnKeyUp() {
        lienzoPanel.onMouseOut();
        lienzoPanel.onBlur();
        lienzoPanel.onKeyUp(KeyboardEvent.Key.DELETE.getUnicharCode());
        verify(keyUpEvent, never()).fire(any(KeyUpEvent.class));

        lienzoPanel.onMouseOut();
        lienzoPanel.onFocus();
        lienzoPanel.onKeyUp(KeyboardEvent.Key.DELETE.getUnicharCode());
        verify(keyUpEvent, never()).fire(any(KeyUpEvent.class));

        lienzoPanel.onMouseOver();
        lienzoPanel.onBlur();
        lienzoPanel.onKeyUp(KeyboardEvent.Key.DELETE.getUnicharCode());
        verify(keyUpEvent, never()).fire(any(KeyUpEvent.class));

        lienzoPanel.onMouseOver();
        lienzoPanel.onFocus();
        lienzoPanel.onKeyUp(KeyboardEvent.Key.DELETE.getUnicharCode());
        verify(keyUpEvent, times(1)).fire(any(KeyUpEvent.class));
    }
}
