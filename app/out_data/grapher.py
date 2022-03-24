import matplotlib.pyplot as plt
import numpy as np

name = "k-adult-1000"

with open(f"compare-{name}.csv") as file:
    lines = [line.split(",") for line in file.read().split("\n")]

if lines[-1] == [""]:
    lines = lines[:-1]

print(lines)

markers = ["o", "X", "^", "s", "P", "D"]
labels = ["castle", "castlel", "bcastle", "FADS", "FADSl", "XBAND"]
colours = ["rebeccapurple", "darkorchid", "hotpink", "royalblue", "dodgerblue", "limegreen"]

for i in range(len(labels)):
    coords = [(int(num), float(loss)) for lab, num, loss in lines if labels[i] == lab]

    x_only, y_only = zip(*coords)
    plt.plot(x_only, y_only, marker=markers[i], label=labels[i].upper(), color=colours[i])

# plt.axis([0, 200, 0, 0.65])
plt.xlim(0, 200)
plt.title("Vary $k$ with $\delta=2k$ on adult-1000")
plt.xlabel("Parameter $k$")
plt.ylabel("Information Loss")
plt.legend(fontsize='small')

plt.savefig(f"{name}.png", dpi=300)