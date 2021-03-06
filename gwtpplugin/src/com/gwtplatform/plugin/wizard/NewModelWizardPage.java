/**
 * Copyright 2011 IMAGEM Solutions TI sant�
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

package com.gwtplatform.plugin.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.gwtplatform.plugin.controls.AddFieldDialog;
import com.gwtplatform.plugin.projectfile.Field;

/**
 *
 * @author Michael Renaud
 *
 */
public class NewModelWizardPage extends NewTypeWizardPage {

  private static final String PAGE_NAME = "NewModelWizardPage";
  private Table table;
  private List<Field> fields;
  private Button addField;
  private Button generateEquals;
  private IStatus fFieldsStatus = new StatusInfo();

  public NewModelWizardPage(IStructuredSelection selection) {
    super(true, PAGE_NAME);
    setTitle("Create a Model");
    setDescription("Create a Model that hold data");

    init(selection);
  }

  // -------- Initialization ---------

  /**
   * The wizard owning this page is responsible for calling this method with the
   * current selection. The selection is used to initialize the fields of the
   * wizard page.
   *
   * @param selection
   *          used to initialize the fields
   */
  protected void init(IStructuredSelection selection) {
    IJavaElement jelem = getInitialJavaElement(selection);
    initContainerPage(jelem);
    initTypePage(jelem);
    doStatusUpdate();
  }

  // ------ validation --------
  private void doStatusUpdate() {
    // status of all used components
    IStatus[] status = new IStatus[] { fContainerStatus, fPackageStatus, fTypeNameStatus,
        fFieldsStatus };

    // the mode severe status will be displayed and the OK button
    // enabled/disabled.
    updateStatus(status);
  }

  /*
   * @see NewContainerWizardPage#handleFieldChanged
   */
  protected void handleFieldChanged(String fieldName) {
    super.handleFieldChanged(fieldName);

    doStatusUpdate();
  }

  @Override
  public void createControl(Composite parent) {
    initializeDialogUnits(parent);

    Composite composite = new Composite(parent, SWT.NONE);
    composite.setFont(parent.getFont());

    int nColumns = 4;

    GridLayout layout = new GridLayout();
    layout.numColumns = nColumns;
    composite.setLayout(layout);

    // pick & choose the wanted UI components
    createContainerControls(composite, nColumns);
    createPackageControls(composite, nColumns);
    // createEnclosingTypeControls(composite, nColumns);

    createSeparator(composite, nColumns);

    createTypeNameControls(composite, nColumns);
    createFieldsControls(composite, nColumns);
    // createGenerateControls(composite, nColumns);

    setControl(composite);
    setFocus();

    Dialog.applyDialogFont(composite);
  }

  protected String getTypeNameLabel() {
    return "Model name:";
  }

  protected IStatus fieldsChanged() {
    StatusInfo status = new StatusInfo();

    if (fields.isEmpty()) {
      status.setError("Models must contain at least one field");
      return status;
    }
    for (int i = 0; i < fields.size() - 1; i++) {
      if (fields.get(i).getName().equals(fields.get(fields.size() - 1).getName())) {
        status.setError("A field named \"" + fields.get(i).getName() + "\" already exists");
        return status;
      }
    }

    Field field = fields.get(fields.size() - 1);
    if (!field.isPrimitiveType() && (field.getType() == null || !field.getType().exists())) {
      status.setError(field.getType().getElementName() + " doesn't exist");
      return status;
    }

    return status;
  }

  protected void createFieldsControls(Composite composite, int nColumns) {
    fields = new ArrayList<Field>();

    Label label = new Label(composite, SWT.NULL);
    label.setText("Model fields:");
    label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

    table = new Table(composite, SWT.BORDER);
    GridData gd = new GridData();
    gd.horizontalAlignment = GridData.FILL;
    gd.horizontalSpan = nColumns - 2;
    gd.heightHint = 100;
    table.setLayoutData(gd);

    TableLayout layout = new TableLayout();
    layout.addColumnData(new ColumnWeightData(50, false));
    layout.addColumnData(new ColumnWeightData(50, false));
    table.setLayout(layout);
    table.setHeaderVisible(true);

    TableColumn fieldType = new TableColumn(table, SWT.LEFT);
    fieldType.setText("Type");
    TableColumn fieldName = new TableColumn(table, SWT.LEFT);
    fieldName.setText("Name");

    Composite buttons = new Composite(composite, SWT.NONE);
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 1;
    buttons.setLayout(gridLayout);
    buttons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    addField = new Button(buttons, SWT.PUSH);
    addField.setText("Add...");
    addField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    addField.addSelectionListener(new SelectionListener() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        addField();
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        addField();
      }
    });

    Button remove = new Button(buttons, SWT.PUSH);
    remove.setText("Remove");
    remove.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    remove.addSelectionListener(new SelectionListener() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        removeField(table.getSelectionIndex());
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        removeField(table.getSelectionIndex());
      }
    });

    fFieldsStatus = fieldsChanged();
    doStatusUpdate();
  }

  protected void addField() {
    AddFieldDialog dialog = new AddFieldDialog(getShell(), getJavaProject(), this);
    dialog.setWindowTitle("Fields edition");
    dialog.setTitle("Add a new field to the model");
    if (dialog.open() == Window.OK) {
      Field result = dialog.getValue();

      TableItem ligne = new TableItem(table, SWT.NONE);
      if (result.isPrimitiveType()) {
        ligne.setText(0, result.getPrimitiveType());
      } else {
        ligne.setText(0, result.getType().getElementName());
      }
      ligne.setText(1, result.getName());

      fields.add(result);
    }
    fFieldsStatus = fieldsChanged();
    addField.setEnabled(fFieldsStatus.isOK());
    doStatusUpdate();
  }

  protected void removeField(int index) {
    if (index != -1) {
      table.remove(index);
      fields.remove(index);
      fFieldsStatus = fieldsChanged();
      addField.setEnabled(fFieldsStatus.isOK());
      doStatusUpdate();
    }
  }

  protected void createGenerateControls(Composite composite, int nColumns) {
    Label label = new Label(composite, SWT.NULL);
    label.setText("Generate:");

    generateEquals = new Button(composite, SWT.CHECK);
    generateEquals.setText("equals() and hashCode()");
    generateEquals.setSelection(true);
  }

  public Field[] getFields() {
    return fields.toArray(new Field[fields.size()]);
  }

  public boolean generateEquals() {
    return generateEquals.getSelection();
  }

}
