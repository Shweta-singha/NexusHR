import type { DepartmentHierarchyNode } from './api'

function TreeNode({ node }: { node: DepartmentHierarchyNode }) {
  return (
    <li>
      <div className="flex items-center gap-2 py-1">
        <span className="font-medium text-slate-800 dark:text-slate-200">{node.name}</span>
        <span className="text-xs text-slate-500 dark:text-slate-400">({node.employeeCount})</span>
      </div>
      {node.children.length > 0 && (
        <ul className="ml-4 border-l border-slate-200 pl-4 dark:border-slate-700">
          {node.children.map((child) => (
            <TreeNode key={child.id} node={child} />
          ))}
        </ul>
      )}
    </li>
  )
}

export default function DepartmentTree({ nodes }: { nodes: DepartmentHierarchyNode[] }) {
  if (nodes.length === 0) {
    return <p className="text-sm text-slate-500 dark:text-slate-400">No departments yet.</p>
  }
  return (
    <ul>
      {nodes.map((node) => (
        <TreeNode key={node.id} node={node} />
      ))}
    </ul>
  )
}
