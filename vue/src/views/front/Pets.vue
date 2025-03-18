<template>
  <div class="main-content">
    <div style="width: 70%; background-color: white; margin: 30px auto; border-radius: 20px">
      <div style="padding-bottom: 10px">
        <div style="display: flex; font-size: 18px; color: #000000FF; line-height: 80px; border-bottom: #cccccc 1px solid;">
          <div style="flex: 3; margin-left: 20px">我的宠物</div>
          <div style="flex: 1; text-align: right; padding-right: 20px">
            <el-button type="warning" round @click="addPet">添加宠物</el-button>
          </div>
        </div>
        <div style="margin: 20px 0; padding: 0 50px">
          <div class="table">
            <el-table :data="petsData" stripe>
              <el-table-column prop="petName" label="宠物名称" width="200px"></el-table-column>
              <el-table-column prop="petKind" label="种类" width="150px"></el-table-column>
              <el-table-column prop="age" label="年龄"></el-table-column>
              <el-table-column prop="health" label="健康状态"></el-table-column>
              <el-table-column prop="info" label="备注"></el-table-column>
              <el-table-column label="操作" align="center" width="180">
                <template v-slot="scope">
                  <el-button size="mini" type="primary" plain @click="editPet(scope.row)">编辑</el-button>
                  <el-button size="mini" type="danger" plain @click="del(scope.row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="pagination" style="margin-top: 20px">
              <el-pagination
                  background
                  @current-change="handleCurrentChange"
                  :current-page="pageNum"
                  :page-sizes="[5, 10, 20]"
                  :page-size="pageSize"
                  layout="total, prev, pager, next"
                  :total="total">
              </el-pagination>
            </div>
          </div>
        </div>
      </div>
    </div>
    <el-dialog :title="form.id ? '编辑宠物' : '添加宠物'" :visible.sync="formVisible" width="40%" :close-on-click-modal="false" destroy-on-close>
      <el-form label-width="100px" style="padding-right: 50px" :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="petName" label="宠物名称">
          <el-input v-model="form.petName" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="petKind" label="种类">
          <el-input v-model="form.petKind" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="age" label="年龄">
          <el-input v-model="form.age" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="health" label="健康状态">
          <el-input v-model="form.health" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="info" label="备注信息">
          <el-input type="textarea" v-model="form.info" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="formVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>

export default {

  data() {
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      petsData: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      formVisible: false,
      form: {},
      rules: {
        petName: [
          {required: true, message: '请输入宠物名称', trigger: 'blur'},
        ],
        petKind: [
          {required: true, message: '请输入宠物种类', trigger: 'blur'},
        ],
        age: [
          {required: true, message: '请输入年龄', trigger: 'blur'},
        ],
        health: [
          {required: true, message: '请输入健康状态', trigger: 'blur'},
        ]
      },
    }
  },
  mounted() {
    this.loadPets(1)
  },
  methods: {
    addPet() {
      this.form = {}
      this.formVisible = true
    },
    editPet(row) {
      this.form = JSON.parse(JSON.stringify(row))
      this.formVisible = true
    },
    save() {
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.form.userId = this.user.id
          this.$request({
            url: this.form.id ? '/pets/update' : '/pets/add',
            method: this.form.id ? 'PUT' : 'POST',
            data: this.form
          }).then(res => {
            if (res.code === '200') {
              this.$message.success('操作成功')
              this.loadPets(1)
              this.formVisible = false
            } else {
              this.$message.error(res.msg)
            }
          })
        }
      })
    },
    loadPets(pageNum) {
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/pets/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          userId: this.user.id
        }
      }).then(res => {
        if (res.code === '200') {
          this.petsData = res.data?.list
          this.total = res.data?.total
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    del(id) {
      this.$request.delete('/pets/delete/' + id).then(res => {
        if (res.code === '200') {
          this.$message.success('删除成功')
          this.loadPets(1)
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    handleCurrentChange(pageNum) {
      this.loadPets(pageNum)
    }
  }
}
</script>
