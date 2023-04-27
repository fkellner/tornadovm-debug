import uk.ac.manchester.tornado.api.ImmutableTaskGraph;
import uk.ac.manchester.tornado.api.TaskGraph;
import uk.ac.manchester.tornado.api.TornadoExecutionPlan;
import uk.ac.manchester.tornado.api.TornadoExecutionResult;
import uk.ac.manchester.tornado.api.annotations.Parallel;
import uk.ac.manchester.tornado.api.enums.DataTransferMode;

public class App
{
  public static void main(String[] args)
  {
    System.out.println("Starting Test");
    int width = 4000;
    int height = 3000;
    float[] samples = new float[width * height];
    // fill with anything
    for(int i = 0; i < samples.length; i++) {
      samples[i] = 1.0f;
    }
    float[] lowPassAtRB = new float[samples.length];
    TaskGraph taskGraph = new TaskGraph("demo");
    taskGraph.transferToDevice(DataTransferMode.FIRST_EXECUTION, samples, lowPassAtRB);
    int green1Idx = 1;
    int green2Idx = 2;
    taskGraph.task("lowPassAtRB", App::calcLowpassAtRB, samples, lowPassAtRB, width, height, green1Idx, green2Idx);
    taskGraph.transferToHost(DataTransferMode.EVERY_EXECUTION, lowPassAtRB);
    // Create an immutable task-graph
    ImmutableTaskGraph immutableTaskGraph = taskGraph.snapshot();

    // Create an execution plan from an immutable task-graph
    TornadoExecutionPlan executionPlan = new TornadoExecutionPlan(immutableTaskGraph);

    // Execute the execution plan
    //TornadoExecutionResult executionResult = 
    executionPlan.execute();
    // System.out.println(executionResult);
    System.out.println(lowPassAtRB[234234]);
    System.out.println("done");


  }

  public static void calcLowpassAtRB(float[] samples, float[] lowPassAtRB, int width, int height, int green1Idx, int green2Idx) {
    for(@Parallel int x = 0; x < width; x++) {
        for(@Parallel int y = 0; y < height; y++) {
            int patternIdx = (x % 2) + 2 * (y % 2);
            if (patternIdx == green1Idx || patternIdx == green2Idx) {
                lowPassAtRB[x + y * width] = 0.0f;
            } else {
                // 3x3 low-pass filter

                float middle = samples[x + y * width];

                float top =         samples[x + max(0, y - 1) * width];
                float bottom =      samples[x + min(height - 1, y + 1) * width];
                float left =        samples[max(0, x - 1) + y * width];
                float right =       samples[min(width - 1, x + 1) + y * width];

                float topLeft =     samples[max(0, x - 1) + max(0, y - 1) * width];
                float topRight =    samples[min(width - 1, x + 1) + max(0, y - 1) * width];
                float bottomLeft =  samples[max(0, x - 1) + min(height - 1, y + 1) * width];
                float bottomRight = samples[min(width - 1, x + 1) + min(height - 1, y + 1) * width];

                lowPassAtRB[x + y * width] = 0.25f * middle +
                        0.125f * (top + bottom + left + right) +
                        0.0625f * (topLeft + topRight + bottomLeft + bottomRight);
            }
        }
    }
  }

  public static float min(float a, float b) {
      return a < b ? a : b;
  }

  public static float max(float a, float b) {
      return a > b ? a : b;
  }

  public static float abs(float n) {
      return n < 0 ? -n : n;
  }

  public static int min(int a, int b) {
      return a < b ? a : b;
  }

  public static int max(int a, int b) {
      return a > b ? a : b;
  }

  public static int abs(int n) {
      return n < 0 ? -n : n;
  }
}
