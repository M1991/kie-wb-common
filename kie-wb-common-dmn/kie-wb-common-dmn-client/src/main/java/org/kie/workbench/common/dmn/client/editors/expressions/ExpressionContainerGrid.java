/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.dmn.client.editors.expressions;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.ait.lienzo.client.core.event.INodeXYEvent;
import com.ait.lienzo.shared.core.types.EventPropagationMode;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.kie.workbench.common.dmn.api.definition.HasExpression;
import org.kie.workbench.common.dmn.api.definition.HasName;
import org.kie.workbench.common.dmn.api.property.dmn.Name;
import org.kie.workbench.common.dmn.client.commands.ClearExpressionTypeCommand;
import org.kie.workbench.common.dmn.client.editors.expressions.types.ExpressionEditorDefinitions;
import org.kie.workbench.common.dmn.client.editors.expressions.types.context.ExpressionCellValue;
import org.kie.workbench.common.dmn.client.editors.expressions.types.context.ExpressionEditorColumn;
import org.kie.workbench.common.dmn.client.resources.i18n.DMNEditorConstants;
import org.kie.workbench.common.dmn.client.widgets.grid.BaseExpressionGrid;
import org.kie.workbench.common.dmn.client.widgets.grid.ExpressionGridCache;
import org.kie.workbench.common.dmn.client.widgets.grid.controls.container.CellEditorControlsView;
import org.kie.workbench.common.dmn.client.widgets.grid.controls.list.HasListSelectorControl;
import org.kie.workbench.common.dmn.client.widgets.grid.controls.list.ListSelectorView;
import org.kie.workbench.common.dmn.client.widgets.grid.model.DMNGridData;
import org.kie.workbench.common.dmn.client.widgets.grid.model.DMNGridRow;
import org.kie.workbench.common.dmn.client.widgets.grid.model.GridCellTuple;
import org.kie.workbench.common.dmn.client.widgets.layer.DMNGridLayer;
import org.kie.workbench.common.stunner.core.client.api.SessionManager;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.command.SessionCommandManager;
import org.uberfire.ext.wires.core.grids.client.model.GridCellValue;
import org.uberfire.ext.wires.core.grids.client.model.GridColumn;
import org.uberfire.ext.wires.core.grids.client.model.impl.BaseHeaderMetaData;
import org.uberfire.ext.wires.core.grids.client.widget.grid.impl.BaseGridWidget;
import org.uberfire.mvp.ParameterizedCommand;

public class ExpressionContainerGrid extends BaseGridWidget implements HasListSelectorControl {

    private static final String COLUMN_GROUP = "ExpressionContainerGrid$Expression0";

    private final CellEditorControlsView.Presenter cellEditorControls;
    private final TranslationService translationService;

    private final SessionManager sessionManager;
    private final SessionCommandManager<AbstractCanvasHandler> sessionCommandManager;
    private final ExpressionGridCache expressionGridCache;
    private final GridCellTuple parent = new GridCellTuple(0, 0, this);

    private String nodeUUID;
    private HasExpression hasExpression;
    private Optional<HasName> hasName = Optional.empty();

    private ExpressionContainerUIModelMapper uiModelMapper;

    public ExpressionContainerGrid(final DMNGridLayer gridLayer,
                                   final CellEditorControlsView.Presenter cellEditorControls,
                                   final TranslationService translationService,
                                   final ListSelectorView.Presenter listSelector,
                                   final SessionManager sessionManager,
                                   final SessionCommandManager<AbstractCanvasHandler> sessionCommandManager,
                                   final Supplier<ExpressionEditorDefinitions> expressionEditorDefinitions,
                                   final ExpressionGridCache expressionGridCache,
                                   final ParameterizedCommand<Optional<HasName>> onHasNameChanged) {
        super(new DMNGridData(),
              gridLayer,
              gridLayer,
              new ExpressionContainerRenderer());
        this.cellEditorControls = cellEditorControls;
        this.translationService = translationService;
        this.sessionManager = sessionManager;
        this.sessionCommandManager = sessionCommandManager;
        this.expressionGridCache = expressionGridCache;

        this.uiModelMapper = new ExpressionContainerUIModelMapper(parent,
                                                                  this::getModel,
                                                                  () -> Optional.ofNullable(hasExpression.getExpression()),
                                                                  () -> nodeUUID,
                                                                  () -> hasExpression,
                                                                  () -> spyHasName(onHasNameChanged),
                                                                  expressionEditorDefinitions,
                                                                  expressionGridCache,
                                                                  listSelector);

        setEventPropagationMode(EventPropagationMode.NO_ANCESTORS);

        final GridColumn expressionColumn = new ExpressionEditorColumn(gridLayer,
                                                                       new BaseHeaderMetaData(COLUMN_GROUP),
                                                                       this);
        expressionColumn.setMovable(false);
        expressionColumn.setResizable(true);

        model.appendColumn(expressionColumn);
        model.appendRow(new DMNGridRow());

        getRenderer().setColumnRenderConstraint((isSelectionLayer, gridColumn) -> !isSelectionLayer || gridColumn.equals(expressionColumn));
    }

    Optional<HasName> spyHasName(final ParameterizedCommand<Optional<HasName>> onHasNameChanged) {
        final Name name = new Name() {
            @Override
            public String getValue() {
                return hasName.orElse(HasName.NOP).getName().getValue();
            }

            @Override
            public void setValue(final String value) {
                hasName.ifPresent(hn -> {
                    hn.getName().setValue(value);
                    onHasNameChanged.execute(hasName);
                });
            }
        };

        return Optional.of(new HasName() {
            @Override
            public Name getName() {
                return name;
            }

            @Override
            public void setName(final Name name) {
                hasName.ifPresent(hn -> {
                    hn.setName(name);
                    onHasNameChanged.execute(hasName);
                });
            }
        });
    }

    @Override
    public boolean onDragHandle(final INodeXYEvent event) {
        return false;
    }

    @Override
    public void deselect() {
        getModel().clearSelections();
        super.deselect();
    }

    public void setExpression(final String nodeUUID,
                              final HasExpression hasExpression,
                              final Optional<HasName> hasName) {
        this.nodeUUID = nodeUUID;
        this.hasExpression = hasExpression;
        this.hasName = hasName;

        uiModelMapper.fromDMNModel(0, 0);

        resizeBasedOnCellExpressionEditor();
    }

    @Override
    public List<ListSelectorItem> getItems(final int uiRowIndex,
                                           final int uiColumnIndex) {
        return Collections.singletonList(ListSelectorTextItem.build(translationService.format(DMNEditorConstants.ExpressionEditor_Clear),
                                                                    true,
                                                                    () -> {
                                                                        cellEditorControls.hide();
                                                                        clearExpressionType();
                                                                    }));
    }

    @Override
    public void onItemSelected(final ListSelectorItem item) {
        final ListSelectorTextItem li = (ListSelectorTextItem) item;
        li.getCommand().execute();
    }

    void clearExpressionType() {
        sessionCommandManager.execute((AbstractCanvasHandler) sessionManager.getCurrentSession().getCanvasHandler(),
                                      new ClearExpressionTypeCommand(parent,
                                                                     nodeUUID,
                                                                     hasExpression,
                                                                     uiModelMapper,
                                                                     expressionGridCache,
                                                                     () -> resizeBasedOnCellExpressionEditor()));
    }

    private void resizeBasedOnCellExpressionEditor() {
        final GridCellValue<?> value = model.getCell(0, 0).getValue();
        if (value instanceof ExpressionCellValue) {
            final Optional<BaseExpressionGrid> grid = ((ExpressionCellValue) value).getValue();
            grid.ifPresent(BaseExpressionGrid::resizeWhenExpressionEditorChanged);
        }
    }
}
