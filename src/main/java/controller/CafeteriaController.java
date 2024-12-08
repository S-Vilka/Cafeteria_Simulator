package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import simu.framework.Trace;
import simu.model.SimulationConstants;
import view.CafeteriaGUI;
import simu.model.MyEngine;
import simu.model.Customer;
import simu.model.SimulationAdjustments;
import simu.model.SimulationConstants;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.List;
import java.util.Arrays;

public class CafeteriaController {
    private CafeteriaGUI mainApp;
    private MyEngine engine;
    private Customer customer;

    @FXML
    private Button lessSimulationSpeed1, moreSimulationSpeed1, lessArrivalRate1, moreArrivalRate1, lessFoodLineSpeed1, moreFoodLineSpeed1, lessCashierSpeed1, moreCashierSpeed1, startButton1, pauseButton1, resumeButton1, preferenceButton1, queueLengthButton1, stopButton1;
    @FXML
    private TextField simulationTime1, delayTime1;
    @FXML
    private Label messageBox, simulationSpeed1, arrivalRate1, foodLineSpeed1, cashierSpeed1, totalStudentsServed, averageTimeSpent, normalFoodLineTimeSpent, veganFoodLineTimeSpent, staffedCashierTimeSpent, selfServiceCashierTimeSpent;
    @FXML
    private BarChart foodlineBarChart;
    @FXML
    private PieChart foodlinePieChart;
    @FXML
    private PieChart servedPieChart;
    @FXML
    private LineChart<Number, Number> servedLineChart;
    @FXML
    private LineChart<Number, Number> averagetimeLineChart;

    //@FXML
    //private CategoryAxis xAxis;

    //@FXML
    //private NumberAxis yAxis;

    public void setMainApp(CafeteriaGUI mainApp) {
        this.mainApp = mainApp;
    }

    public CafeteriaController() {
        this.engine = new MyEngine();
    }

    private void setButtonsDisabled(boolean disabled) {
        lessSimulationSpeed1.setDisable(disabled);
        moreSimulationSpeed1.setDisable(disabled);
        lessArrivalRate1.setDisable(disabled);
        moreArrivalRate1.setDisable(disabled);
        lessFoodLineSpeed1.setDisable(disabled);
        moreFoodLineSpeed1.setDisable(disabled);
        lessCashierSpeed1.setDisable(disabled);
        moreCashierSpeed1.setDisable(disabled);
        startButton1.setDisable(disabled);
        pauseButton1.setDisable(disabled);
        resumeButton1.setDisable(disabled);
        stopButton1.setDisable(disabled);
        preferenceButton1.setDisable(disabled);
        queueLengthButton1.setDisable(disabled);
    }

    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean validateInputs() {
        boolean valid = true;
        if (!isInteger(simulationTime1.getText()) || !isInteger(delayTime1.getText())) {
            messageBox.setText("Simulation Time and Delay Time must be an integer.");
            valid = false;
        } else if (Integer.parseInt(simulationTime1.getText()) < 1 || Integer.parseInt(delayTime1.getText()) < 1) {
            messageBox.setText("Simulation Time and Delay Time must be greater than 0.");
            valid = false;
        } else if (!preferenceButton1.isDisable() && !queueLengthButton1.isDisable()) {
            messageBox.setText("Please select Choosing Type.");
            valid = false;
        }
        return valid;
    }

    @FXML
    private void preferenceButtonAction() {
        preferenceButton1.setDisable(true);
        queueLengthButton1.setDisable(false);
        resumeButton1.setDisable(true);
        engine.setAssignByQueueLength(false);
        checkStartConditions();
    }

    @FXML
    private void queueLengthButtonAction() {
        queueLengthButton1.setDisable(true);
        preferenceButton1.setDisable(false);
        resumeButton1.setDisable(true);
        engine.setAssignByQueueLength(true);
        checkStartConditions();
    }

    private void checkStartConditions() {
        if (validateInputs()) {
            startButton1.setDisable(false);
            messageBox.setText("Press START to begin the simulation.");
        }
    }

    @FXML
    private void startButtonAction() throws InterruptedException {
        if (validateInputs()) {
            setButtonsDisabled(false);
            startButton1.setDisable(true);
            preferenceButton1.setDisable(true);
            queueLengthButton1.setDisable(true);
            pauseButton1.setDisable(false);
            stopButton1.setDisable(false);
            resumeButton1.setDisable(true);
            messageBox.setText("Simulation started. Use PAUSE, RESUME, and RESTART as needed.");

            engine.setSimulationTime(Double.parseDouble(simulationTime1.getText()));
            engine.setDelayTime(Double.parseDouble(delayTime1.getText()));
            Trace.setTraceLevel(Trace.Level.INFO);

            new Thread(() -> {
                engine.startSimulation();
            }).start();

            // BarChart
            XYChart.Series<String, Number> dataSeries1 = new XYChart.Series<>();
            //dataSeries1.setName("Food Lines");
            dataSeries1.getData().add(new XYChart.Data<>("Normal Food Line", 0));
            dataSeries1.getData().add(new XYChart.Data<>("Vegan Food Line", 0));
            dataSeries1.getData().add(new XYChart.Data<>("Staffed Cashier", 0));
            dataSeries1.getData().add(new XYChart.Data<>("Self Service Cashier", 0));

            foodlineBarChart.getData().add(dataSeries1);


            // makes LineChart line, not dots
            servedLineChart.setCreateSymbols(false);
            servedLineChart.setAnimated(true);
            averagetimeLineChart.setCreateSymbols(false);
            averagetimeLineChart.setAnimated(true);

            // LineChart for Total Students Served
            XYChart.Series<Number, Number> series1 = new XYChart.Series<>();
            series1.setName("The Number of Customers Served");
            series1.getData().add(new XYChart.Data<>(0, 0));
            servedLineChart.getData().add(series1);

            int[] iterationStep = {0};

            // LineChart for Average Time Spent
            XYChart.Series<Number, Number> series2 = new XYChart.Series<>();
            series2.setName("Average Time Spent in the Cafeteria Service Points");
            series2.getData().add(new XYChart.Data<>(0, 0));
            averagetimeLineChart.getData().add(series2);


            //PieChart FoodLine
            PieChart.Data slice1 = new PieChart.Data("Normal Food Line", 0);
            PieChart.Data slice2 = new PieChart.Data("Vegan Food Line"  , 0);
            PieChart.Data slice3 = new PieChart.Data("Staffed Cashier" , 0);
            PieChart.Data slice4 = new PieChart.Data("Self Service Cashier" , 0);

            foodlinePieChart.getData().add(slice1);
            foodlinePieChart.getData().add(slice2);
            foodlinePieChart.getData().add(slice3);
            foodlinePieChart.getData().add(slice4);


            //PieChart Served
            PieChart.Data slice5 = new PieChart.Data("Served Customers", 0);
            PieChart.Data slice6 = new PieChart.Data("Total Number of Customers Served"  , 0);

            servedPieChart.getData().add(slice5);
            servedPieChart.getData().add(slice6);




            // Create a Timeline to update the GUI elements periodically
            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), event -> {
                if (engine.isRunning() && !engine.isStopped()) {

                    // BarChart
                    dataSeries1.getData().get(0).setYValue(SimulationConstants.AVG_NON_VEGAN_SERVICE_TIME);
                    dataSeries1.getData().get(1).setYValue(SimulationConstants.AVG_VEGAN_SERVICE_TIME);
                    dataSeries1.getData().get(2).setYValue(SimulationConstants.AVG_CASHIER_SERVICE_TIME);
                    dataSeries1.getData().get(3).setYValue(SimulationConstants.AVG_SELF_CHECKOUT_SERVICE_TIME);

                    // LineChart Customers Served
                    series1.getData().add(new XYChart.Data<>(iterationStep[0], SimulationConstants.TOTAL_CUSTOMERS_SERVED));
                    iterationStep[0]++;

                    // LineChart for Average Time
                    series2.getData().add(new XYChart.Data<>(iterationStep[0], SimulationConstants.AVERAGE_TIME_SPENT));
                    iterationStep[0]++;

                    // PieChart FoodLine
                    slice1.setPieValue(SimulationConstants.AVG_NON_VEGAN_SERVICE_TIME);
                    slice2.setPieValue(SimulationConstants.AVG_VEGAN_SERVICE_TIME);
                    slice3.setPieValue(SimulationConstants.AVG_CASHIER_SERVICE_TIME);
                    slice4.setPieValue(SimulationConstants.AVG_SELF_CHECKOUT_SERVICE_TIME);

                    //PieChart Served Customers
                    slice5.setPieValue(SimulationConstants.TOTAL_CUSTOMERS_SERVED);
                    slice6.setPieValue(customer.getTotalCustomers());

                }
            }));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();

        }
    }

    @FXML
    private void pauseButtonAction() {
        pauseButton1.setDisable(true);
        resumeButton1.setDisable(false);
        stopButton1.setDisable(false);
        messageBox.setText("Simulation paused. Press RESUME to continue.");
        engine.pauseSimulation();
    }

    @FXML
    private void resumeButtonAction() {
        resumeButton1.setDisable(true);
        pauseButton1.setDisable(false);
        preferenceButton1.setDisable(true);
        queueLengthButton1.setDisable(true);
        stopButton1.setDisable(false);
        messageBox.setText("Simulation resumed.");
        engine.resumeSimulation();
    }

    @FXML
    private void stopButtonAction() {
        setButtonsDisabled(true);
        preferenceButton1.setDisable(false);
        queueLengthButton1.setDisable(false);
        messageBox.setText("Simulation stopped. Press START to start a new simulation.");
        engine.stopSimulation();
        engine.resetVariables();
        /*totalStudentsServed.setText(String.format("%d", SimulationConstants.TOTAL_CUSTOMERS_SERVED));
        averageTimeSpent.setText(String.format("%.2f", SimulationConstants.AVERAGE_TIME_SPENT));
        normalFoodLineTimeSpent.setText(String.format("%.2f", SimulationConstants.AVG_NON_VEGAN_SERVICE_TIME));
        veganFoodLineTimeSpent.setText(String.format("%.2f", SimulationConstants.AVG_VEGAN_SERVICE_TIME));
        staffedCashierTimeSpent.setText(String.format("%.2f", SimulationConstants.AVG_CASHIER_SERVICE_TIME));
        selfServiceCashierTimeSpent.setText(String.format("%.2f", SimulationConstants.AVG_SELF_CHECKOUT_SERVICE_TIME)); */

    }

    public void initialStartButtonAction() {
        System.out.println("The initialStartButton has been pressed");
        try {
            mainApp.loadScene("/simulationpage.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void lessSimulationSpeedAction() {
        System.out.println("The lessSimulationSpeedAction button has been pressed");
        SimulationAdjustments.setAdjustStimulationSpeedFlag(false);

    }

    public void moreSimulationSpeedAction() {
        System.out.println("The moreSimulationSpeedAction button has been pressed");
        SimulationAdjustments.setAdjustStimulationSpeedFlag(true);
    }

    public void lessArrivalRateAction() {
        System.out.println("The lessArrivalRateAction button has been pressed");
        SimulationAdjustments.setAdjustStudentArrivalFlag(false);
    }

    public void moreArrivalRateAction() {
        System.out.println("The moreArrivalRateAction button has been pressed");
        SimulationAdjustments.setAdjustStudentArrivalFlag(true);
    }

    public void lessFoodLineSpeedAction() {
        System.out.println("The lessFoodLineSpeedAction button has been pressed");
        SimulationAdjustments.setAdjustFoodLineServiceSpeedFlag(false);
    }

    public void moreFoodLineSpeedAction() {
        System.out.println("The moreFoodLineSpeedAction button has been pressed");
        SimulationAdjustments.setAdjustFoodLineServiceSpeedFlag(true);
    }

    public void lessCashierSpeedAction() {
        System.out.println("The lessCashierSpeedAction button has been pressed");
        SimulationAdjustments.setAdjustCashierServiceSpeedFlag(false);
    }

    public void moreCashierSpeedAction() {
        System.out.println("The moreCashierSpeedAction button has been pressed");
        SimulationAdjustments.setAdjustCashierServiceSpeedFlag(true);
    }
}